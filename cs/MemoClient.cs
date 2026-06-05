using System;
using System.Collections.Generic;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using System.Web.Script.Serialization;

namespace MemoClient
{
    public class MemoForm : Form
    {
        private const string ApiUrl = "http://localhost:8080/memos";

        private static readonly HttpClient client = new HttpClient();
        private readonly JavaScriptSerializer serializer = new JavaScriptSerializer();

        private DataGridView memoGrid;
        private TextBox inputBox;

        private ContextMenuStrip memoMenu;
        private ToolStripMenuItem editMenuItem;
        private ToolStripMenuItem copyMenuItem;
        private ToolStripMenuItem deleteMenuItem;

        private bool isLoading = false;

        public MemoForm()
        {
            Text = "Memo Client";
            Width = 760;
            Height = 520;
            StartPosition = FormStartPosition.CenterScreen;
            KeyPreview = true;

            InitializeComponents();
            WireEvents();
        }

        protected override bool ProcessCmdKey(ref Message msg, Keys keyData)
        {
            if (keyData == Keys.Insert)
            {
                inputBox.Focus();
                inputBox.SelectAll();
                return true;
            }

            if (keyData == Keys.Escape)
            {
                FocusMemoGrid();
                return true;
            }

            return base.ProcessCmdKey(ref msg, keyData);
        }

        private void InitializeComponents()
        {
            memoGrid = new DataGridView();
            memoGrid.Dock = DockStyle.Fill;
            memoGrid.AllowUserToAddRows = false;
            memoGrid.AllowUserToDeleteRows = false;
            memoGrid.AllowUserToResizeRows = false;
            memoGrid.RowHeadersVisible = false;
            memoGrid.ColumnHeadersVisible = false;
            memoGrid.MultiSelect = false;
            memoGrid.SelectionMode = DataGridViewSelectionMode.CellSelect;
            memoGrid.AutoGenerateColumns = false;
            memoGrid.EditMode = DataGridViewEditMode.EditOnKeystrokeOrF2;

            DataGridViewTextBoxColumn idColumn = new DataGridViewTextBoxColumn();
            idColumn.Name = "Id";
            idColumn.HeaderText = "ID";
            idColumn.Visible = false;
            idColumn.ReadOnly = true;

            DataGridViewTextBoxColumn contentColumn = new DataGridViewTextBoxColumn();
            contentColumn.Name = "Content";
            contentColumn.HeaderText = "メモ内容";
            contentColumn.AutoSizeMode = DataGridViewAutoSizeColumnMode.Fill;
            contentColumn.ReadOnly = false;

            memoGrid.Columns.Add(idColumn);
            memoGrid.Columns.Add(contentColumn);

            inputBox = new TextBox();
            inputBox.Dock = DockStyle.Bottom;

            memoMenu = new ContextMenuStrip();
            editMenuItem = new ToolStripMenuItem("編集");
            copyMenuItem = new ToolStripMenuItem("コピー");
            deleteMenuItem = new ToolStripMenuItem("削除");

            memoMenu.Items.Add(editMenuItem);
            memoMenu.Items.Add(copyMenuItem);
            memoMenu.Items.Add(deleteMenuItem);
            memoGrid.ContextMenuStrip = memoMenu;

            Controls.Add(memoGrid);
            Controls.Add(inputBox);
        }

        private void WireEvents()
        {
            Load += MemoForm_Load;

            inputBox.KeyDown += InputBox_KeyDown;

            memoGrid.CellEndEdit += MemoGrid_CellEndEdit;
            memoGrid.KeyDown += MemoGrid_KeyDown;
            memoGrid.CellMouseDown += MemoGrid_CellMouseDown;

            editMenuItem.Click += EditMenuItem_Click;
            copyMenuItem.Click += CopyMenuItem_Click;
            deleteMenuItem.Click += DeleteMenuItem_Click;
        }

        private async void MemoForm_Load(object sender, EventArgs e)
        {
            await LoadMemosAsync(0);
        }

        private async void InputBox_KeyDown(object sender, KeyEventArgs e)
        {
            if (e.KeyCode == Keys.Enter)
            {
                e.SuppressKeyPress = true;
                await AddMemoFromInputAsync();
            }
        }

        private async void MemoGrid_CellEndEdit(object sender, DataGridViewCellEventArgs e)
        {
            if (isLoading)
            {
                return;
            }

            if (e.RowIndex < 0 || e.ColumnIndex < 0)
            {
                return;
            }

            if (memoGrid.Columns[e.ColumnIndex].Name == "Content")
            {
                await UpdateMemoAsync(e.RowIndex);
            }
        }

        private async void MemoGrid_KeyDown(object sender, KeyEventArgs e)
        {
            if (e.Control && e.KeyCode == Keys.C)
            {
                e.SuppressKeyPress = true;
                CopyCurrentMemo();
                return;
            }

            if (e.Control && e.KeyCode == Keys.Insert)
            {
                e.SuppressKeyPress = true;
                CopyCurrentMemo();
                return;
            }

            if (e.Control && e.KeyCode == Keys.V)
            {
                e.SuppressKeyPress = true;
                await AddMemoFromClipboardAsync();
                return;
            }

            if (e.Shift && e.KeyCode == Keys.Insert)
            {
                e.SuppressKeyPress = true;
                await AddMemoFromClipboardAsync();
                return;
            }

            if (e.KeyCode == Keys.Delete)
            {
                e.SuppressKeyPress = true;
                await DeleteCurrentMemoAsync();
                return;
            }
        }

        private void MemoGrid_CellMouseDown(object sender, DataGridViewCellMouseEventArgs e)
        {
            if (e.Button != MouseButtons.Right)
            {
                return;
            }

            if (e.RowIndex < 0)
            {
                return;
            }

            memoGrid.CurrentCell = memoGrid.Rows[e.RowIndex].Cells["Content"];
        }

        private void EditMenuItem_Click(object sender, EventArgs e)
        {
            BeginEditCurrentMemo();
        }

        private void CopyMenuItem_Click(object sender, EventArgs e)
        {
            CopyCurrentMemo();
        }

        private async void DeleteMenuItem_Click(object sender, EventArgs e)
        {
            await DeleteCurrentMemoAsync();
        }

        private async Task LoadMemosAsync(int keepId)
        {
            try
            {
                isLoading = true;

                string json = await client.GetStringAsync(ApiUrl);
                List<MemoDto> memos = serializer.Deserialize<List<MemoDto>>(json);

                if (memos == null)
                {
                    memos = new List<MemoDto>();
                }

                memoGrid.Rows.Clear();

                foreach (MemoDto memo in memos)
                {
                    memoGrid.Rows.Add(
                        memo.id,
                        NullToEmpty(memo.content)
                    );
                }

                RestoreCurrentCell(keepId);
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message, "読み込みエラー");
            }
            finally
            {
                isLoading = false;
            }
        }

        private void RestoreCurrentCell(int keepId)
        {
            if (memoGrid.Rows.Count == 0)
            {
                return;
            }

            int rowIndex = 0;

            if (keepId > 0)
            {
                for (int i = 0; i < memoGrid.Rows.Count; i++)
                {
                    if (GetRowId(memoGrid.Rows[i]) == keepId)
                    {
                        rowIndex = i;
                        break;
                    }
                }
            }

            memoGrid.CurrentCell = memoGrid.Rows[rowIndex].Cells["Content"];
        }

        private void FocusMemoGrid()
        {
            if (memoGrid.Rows.Count > 0 && memoGrid.CurrentCell == null)
            {
                memoGrid.CurrentCell = memoGrid.Rows[0].Cells["Content"];
            }

            memoGrid.Focus();
        }

        private async Task AddMemoFromInputAsync()
        {
            string content = inputBox.Text.Trim();

            if (content.Length == 0)
            {
                MessageBox.Show("メモを入力してください。", "入力エラー");
                return;
            }

            try
            {
                await PostMemoAsync(content);
                inputBox.Clear();
                await LoadMemosAsync(0);
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message, "追加エラー");
            }
        }

        private async Task AddMemoFromClipboardAsync()
        {
            string content = Clipboard.GetText().Trim();

            if (content.Length == 0)
            {
                MessageBox.Show("クリップボードに追加できる文字列がありません。", "入力エラー");
                return;
            }

            try
            {
                await PostMemoAsync(content);
                await LoadMemosAsync(0);
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message, "追加エラー");
            }
        }

        private async Task UpdateMemoAsync(int rowIndex)
        {
            DataGridViewRow row = memoGrid.Rows[rowIndex];

            int id = GetRowId(row);
            string content = GetRowString(row, "Content").Trim();

            if (id <= 0)
            {
                return;
            }

            if (content.Length == 0)
            {
                MessageBox.Show("メモ内容は空にできません。", "入力エラー");
                await LoadMemosAsync(id);
                return;
            }

            bool reloadAfterCatch = false;

            try
            {
                await PutMemoAsync(id, content);
                await LoadMemosAsync(id);
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message, "更新エラー");
                reloadAfterCatch = true;
            }

            if (reloadAfterCatch)
            {
                await LoadMemosAsync(id);
            }
        }

        private async Task DeleteCurrentMemoAsync()
        {
            if (memoGrid.CurrentCell == null)
            {
                return;
            }

            DataGridViewRow row = memoGrid.Rows[memoGrid.CurrentCell.RowIndex];
            int id = GetRowId(row);

            if (id <= 0)
            {
                return;
            }

            bool reloadAfterCatch = false;

            try
            {
                HttpResponseMessage response = await client.DeleteAsync(ApiUrl + "/" + id);
                await EnsureSuccessAsync(response);
                await LoadMemosAsync(0);
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message, "削除エラー");
                reloadAfterCatch = true;
            }

            if (reloadAfterCatch)
            {
                await LoadMemosAsync(0);
            }
        }

        private void BeginEditCurrentMemo()
        {
            if (memoGrid.CurrentCell == null)
            {
                return;
            }

            DataGridViewRow row = memoGrid.Rows[memoGrid.CurrentCell.RowIndex];
            memoGrid.CurrentCell = row.Cells["Content"];
            memoGrid.BeginEdit(true);
        }

        private void CopyCurrentMemo()
        {
            if (memoGrid.CurrentCell == null)
            {
                return;
            }

            DataGridViewRow row = memoGrid.Rows[memoGrid.CurrentCell.RowIndex];
            string content = GetRowString(row, "Content");

            if (content.Length == 0)
            {
                return;
            }

            Clipboard.SetText(content);
        }

        private async Task PostMemoAsync(string content)
        {
            MemoRequestDto request = new MemoRequestDto();
            request.content = content;

            string json = serializer.Serialize(request);

            StringContent body = new StringContent(
                json,
                Encoding.UTF8,
                "application/json"
            );

            HttpResponseMessage response = await client.PostAsync(ApiUrl, body);
            await EnsureSuccessAsync(response);
        }

        private async Task PutMemoAsync(int id, string content)
        {
            MemoRequestDto request = new MemoRequestDto();
            request.content = content;

            string json = serializer.Serialize(request);

            StringContent body = new StringContent(
                json,
                Encoding.UTF8,
                "application/json"
            );

            HttpResponseMessage response = await client.PutAsync(ApiUrl + "/" + id, body);
            await EnsureSuccessAsync(response);
        }

        private async Task EnsureSuccessAsync(HttpResponseMessage response)
        {
            if (response.IsSuccessStatusCode)
            {
                return;
            }

            string message = await response.Content.ReadAsStringAsync();

            if (message == null || message.Length == 0)
            {
                message = response.StatusCode.ToString();
            }

            throw new Exception(message);
        }

        private int GetRowId(DataGridViewRow row)
        {
            object value = row.Cells["Id"].Value;

            if (value == null)
            {
                return 0;
            }

            int id;

            if (int.TryParse(value.ToString(), out id))
            {
                return id;
            }

            return 0;
        }

        private string GetRowString(DataGridViewRow row, string columnName)
        {
            object value = row.Cells[columnName].Value;

            if (value == null)
            {
                return "";
            }

            return value.ToString();
        }

        private string NullToEmpty(string value)
        {
            if (value == null)
            {
                return "";
            }

            return value;
        }
    }

    public class MemoDto
    {
        public int id { get; set; }
        public string content { get; set; }
    }

    public class MemoRequestDto
    {
        public string content { get; set; }
    }

    internal static class Program
    {
        [STAThread]
        private static void Main()
        {
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            Application.Run(new MemoForm());
        }
    }
}
