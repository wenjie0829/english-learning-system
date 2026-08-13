<template>
  <div class="admin-page">
    <header class="top-bar">
      <div class="top-bar-inner">
        <div class="brand" @click="router.push('/')">
          <div class="brand-mark"><span>词</span></div>
          <span class="brand-name dict-heading">管理员后台</span>
        </div>
        <div class="user-zone">
          <span class="user-greeting">{{ userStore.user?.username }} · 管理员</span>
          <button class="ghost-btn" @click="router.push('/')">返回主页</button>
        </div>
      </div>
    </header>

    <main class="main-content">
      <!-- 统计条 -->
      <section class="stat-row">
        <div class="mini-stat">
          <span class="mini-stat-label">总用户数</span>
          <span class="mini-stat-value">{{ stats.totalUsers ?? '—' }}</span>
        </div>
        <div class="mini-stat">
          <span class="mini-stat-label">管理员</span>
          <span class="mini-stat-value" style="color: var(--color-primary)">{{ stats.adminCount ?? '—' }}</span>
        </div>
        <div class="mini-stat">
          <span class="mini-stat-label">已封禁</span>
          <span class="mini-stat-value" style="color: var(--color-rust)">{{ stats.disabledCount ?? '—' }}</span>
        </div>
        <div class="mini-stat">
          <span class="mini-stat-label">总单词数</span>
          <span class="mini-stat-value" style="color: var(--color-moss)">{{ stats.totalWords ?? '—' }}</span>
        </div>
        <div class="mini-stat">
          <span class="mini-stat-label">总例句数</span>
          <span class="mini-stat-value" style="color: var(--color-amber)">{{ stats.totalExampleSentences ?? '—' }}</span>
        </div>
      </section>

      <!-- Tab 切换 -->
      <div class="tab-strip">
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'users' }"
          @click="activeTab = 'users'"
        >用户管理</button>
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'words' }"
          @click="activeTab = 'words'"
        >单词库管理</button>
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'import' }"
          @click="activeTab = 'import'"
        >单词书导入</button>
      </div>

      <!-- ============ 用户管理 ============ -->
      <section v-if="activeTab === 'users'" class="panel">
        <el-table :data="users" v-loading="usersLoading" style="width: 100%">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="username" label="用户名" width="160" />
          <el-table-column prop="email" label="邮箱" min-width="180">
            <template #default="{ row }">{{ row.email || '—' }}</template>
          </el-table-column>
          <el-table-column label="角色" width="160">
            <template #default="{ row }">
              <el-select
                v-model="row.role"
                size="small"
                style="width: 120px"
                @change="(val) => onRoleChange(row, val)"
              >
                <el-option label="学生 STUDENT" value="STUDENT" />
                <el-option label="管理员 ADMIN" value="ADMIN" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <span class="status-pill" :class="row.enabled ? 'ok' : 'banned'">
                {{ row.enabled ? '正常' : '已封禁' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="注册时间" width="180">
            <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button
                size="small"
                :type="row.enabled ? 'warning' : 'success'"
                text
                @click="onToggleStatus(row)"
              >{{ row.enabled ? '封禁' : '解封' }}</el-button>
              <el-button size="small" type="danger" text @click="onDeleteUser(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <!-- ============ 单词库管理 ============ -->
      <section v-if="activeTab === 'words'" class="panel">
        <div class="panel-toolbar">
          <el-input
            v-model="wordSearchKeyword"
            placeholder="按单词/释义搜索"
            style="width: 240px"
            clearable
          />
          <div style="display: flex; gap: 12px; align-items: center">
            <span v-if="selectedWordIds.length" style="font-size: 13px; color: var(--color-ink-soft)">
              已选中 {{ selectedWordIds.length }} 项
            </span>
            <el-button
              v-if="selectedWordIds.length"
              type="danger"
              size="small"
              @click="onBatchDeleteWords"
            >批量删除</el-button>
            <button class="primary-btn small" @click="openWordDialog(null)">+ 新增单词</button>
          </div>
        </div>

        <el-table
          :data="filteredWords"
          v-loading="wordsLoading"
          style="width: 100%"
          row-key="id"
          @selection-change="onWordSelectionChange"
        >
          <el-table-column type="selection" width="46" />
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="word" label="单词" width="140" />
          <el-table-column prop="phonetic" label="音标" width="120">
            <template #default="{ row }">{{ row.phonetic || '—' }}</template>
          </el-table-column>
          <el-table-column prop="definition" label="释义" min-width="200" show-overflow-tooltip />
          <el-table-column prop="difficultyLevel" label="难度" width="100">
            <template #default="{ row }">
              <span class="status-pill" :class="difficultyClass(row.difficultyLevel)">
                {{ difficultyLabel(row.difficultyLevel) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="230" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="primary" text @click="openWordDialog(row)">编辑</el-button>
              <el-button size="small" text @click="openExampleDialog(row)">例句</el-button>
              <el-button size="small" type="danger" text @click="onDeleteWord(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <!-- ============ 单词书导入 ============ -->
      <section v-if="activeTab === 'import'" class="panel">
        <div class="import-mode-switch">
          <button
            class="mode-btn"
            :class="{ active: importMode === 'file' }"
            @click="importMode = 'file'"
          >上传文件</button>
          <button
            class="mode-btn"
            :class="{ active: importMode === 'text' }"
            @click="importMode = 'text'"
          >粘贴文本</button>
        </div>

        <div class="ai-toggle">
          <el-switch v-model="useAi" />
          <span class="ai-toggle-label">
            使用 AI 智能解析
            <span class="ai-toggle-hint">（排版再乱也能读懂，但更慢，且需要已配置 DeepSeek API Key；不开则用免费的规则匹配，速度快但对排版要求较高）</span>
          </span>
        </div>

        <div v-if="importMode === 'file'" class="import-upload">
          <el-upload
            drag
            :auto-upload="false"
            :show-file-list="false"
            accept=".pdf,.txt,.docx"
            @change="onImportFileChange"
          >
            <div class="upload-inner">
              <el-icon :size="32" style="color: var(--color-ink-soft)"><UploadFilled /></el-icon>
              <p>{{ importFile ? importFile.name : '点击或拖拽单词书文件到这里（支持 PDF / TXT / Word）' }}</p>
            </div>
          </el-upload>
          <button class="primary-btn small" :disabled="!importFile || parsing" @click="parseImportFile">
            {{ parsing ? '解析中…' : '解析文件' }}
          </button>
        </div>

        <div v-else class="import-paste">
          <el-input
            v-model="pasteText"
            type="textarea"
            :rows="8"
            placeholder="把单词表文字粘贴到这里，每行一个单词，例如：&#10;abandon /əˈbændən/ v. 放弃；抛弃&#10;ability n. 能力"
          />
          <button class="primary-btn small" :disabled="!pasteText.trim() || parsing" @click="parsePastedText">
            {{ parsing ? '解析中…' : '解析文本' }}
          </button>
        </div>

        <p class="import-hint">
          支持 PDF、TXT 纯文本、Word（.docx）三种文件格式，也可以直接粘贴文本。解析结果仅供参考（排版千差万别，可能有漏识别或识别错误），
          请在下方核对/编辑后再确认导入。已存在于词库的单词默认不勾选，避免重复。
          <br />
          <strong>小贴士</strong>：如果上传文件解析效果不好（比如原始材料排版比较复杂），可以先自己在别处把文字整理成"一行一个单词"的样子，再用"粘贴文本"这个方式导入，成功率会高很多。
        </p>

        <div v-if="importItems.length" class="import-result">
          <div v-if="aiMeta && aiMeta.truncated" class="ai-truncated-hint">
            ⚠️ 文档内容较多，本次只处理了前 {{ aiMeta.processedChunks }} / {{ aiMeta.totalChunks }} 段。
            如果需要处理完整文档，建议把文件拆小一点分批导入。
          </div>
          <div v-if="aiMeta && aiMeta.failedChunks > 0" class="ai-truncated-hint">
            ⚠️ 有 {{ aiMeta.failedChunks }} 段调用 AI 解析时失败（可能是网络问题或触发了限流），这部分内容没有被解析。
          </div>
          <div class="import-toolbar">
            <span>共解析出 {{ importItems.length }} 条，已选中 {{ selectedImportCount }} 条</span>
            <button class="primary-btn small" :disabled="importing || !selectedImportCount" @click="confirmImportSelected">
              {{ importing ? '导入中…' : '确认导入选中项' }}
            </button>
          </div>

          <el-table :data="importItems" max-height="420" style="width: 100%">
            <el-table-column width="50">
              <template #default="{ row }">
                <el-checkbox v-model="row.selected" />
              </template>
            </el-table-column>
            <el-table-column label="单词" width="140">
              <template #default="{ row }"><el-input v-model="row.word" size="small" /></template>
            </el-table-column>
            <el-table-column label="音标" width="130">
              <template #default="{ row }"><el-input v-model="row.phonetic" size="small" /></template>
            </el-table-column>
            <el-table-column label="词性" width="90">
              <template #default="{ row }"><el-input v-model="row.partOfSpeech" size="small" /></template>
            </el-table-column>
            <el-table-column label="释义" min-width="200">
              <template #default="{ row }"><el-input v-model="row.definition" size="small" /></template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <span class="status-pill" :class="row.alreadyExists ? 'warn' : 'ok'">
                  {{ row.alreadyExists ? '已存在' : '新单词' }}
                </span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </section>
    </main>

    <!-- 新增/编辑单词弹窗 -->
    <el-dialog v-model="wordDialogVisible" :title="editingWord ? '编辑单词' : '新增单词'" width="480px">
      <el-form :model="wordForm" label-position="top">
        <el-form-item label="单词" required>
          <el-input v-model="wordForm.word" placeholder="例如 abandon" />
        </el-form-item>
        <el-form-item label="音标">
          <el-input v-model="wordForm.phonetic" placeholder="例如 /əˈbændən/" />
        </el-form-item>
        <el-form-item label="中文释义" required>
          <el-input v-model="wordForm.definition" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="AI 详细释义（选填）">
          <el-input v-model="wordForm.aiDefinition" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="词性">
          <el-input v-model="wordForm.partOfSpeech" placeholder="例如 v. / n. / adj." style="width: 160px" />
        </el-form-item>
        <el-form-item label="难度等级">
          <el-select v-model="wordForm.difficultyLevel" style="width: 160px">
            <el-option label="简单" value="EASY" />
            <el-option label="中等" value="MEDIUM" />
            <el-option label="困难" value="HARD" />
          </el-select>
        </el-form-item>
        <el-form-item label="发音音频 URL（选填）">
          <el-input v-model="wordForm.audioUrl" placeholder="https://..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <button class="ghost-btn" @click="wordDialogVisible = false">取消</button>
        <button class="primary-btn small" @click="saveWord" :disabled="wordSaving">
          {{ wordSaving ? '保存中…' : '保存' }}
        </button>
      </template>
    </el-dialog>

    <!-- 例句管理弹窗 -->
    <el-dialog v-model="exampleDialogVisible" :title="`例句管理 · ${exampleTargetWord?.word || ''}`" width="560px">
      <div class="example-list">
        <div v-if="!examples.length" class="empty-hint">还没有例句，在下面添加一条吧</div>
        <div v-for="ex in examples" :key="ex.id" class="example-item">
          <div class="example-text">
            <p class="en">{{ ex.sentence }}</p>
            <p v-if="ex.translation" class="zh">{{ ex.translation }}</p>
          </div>
          <el-button size="small" type="danger" text @click="onDeleteExample(ex)">删除</el-button>
        </div>
      </div>

      <el-form :model="exampleForm" label-position="top" class="example-form">
        <el-form-item label="英文例句">
          <el-input v-model="exampleForm.sentence" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="中文翻译（选填）">
          <el-input v-model="exampleForm.translation" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <button class="ghost-btn" @click="exampleDialogVisible = false">关闭</button>
        <button class="primary-btn small" @click="addExample" :disabled="exampleSaving">
          {{ exampleSaving ? '添加中…' : '添加例句' }}
        </button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import {
  getSystemStatistics,
  getAllUsers,
  updateUserRole,
  updateUserStatus,
  deleteUser,
  getAdminWords,
  createWord as apiCreateWord,
  updateWord as apiUpdateWord,
  deleteWord as apiDeleteWord,
  batchDeleteWords,
  getExampleSentences,
  addExampleSentence,
  deleteExampleSentence,
  parsePdfImport,
  confirmPdfImport,
  parseTextImport,
  parseAiFileImport,
  parseAiTextImport
} from '@/api/admin'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('users')
const stats = ref({})

// ---------- 用户管理 ----------
const users = ref([])
const usersLoading = ref(false)

const loadUsers = async () => {
  usersLoading.value = true
  try {
    users.value = await getAllUsers()
  } catch (e) {
    console.error(e)
  } finally {
    usersLoading.value = false
  }
}

const onRoleChange = async (row, newRole) => {
  try {
    await updateUserRole(row.id, newRole)
    ElMessage.success('角色已更新')
  } catch (e) {
    ElMessage.error('更新失败')
    loadUsers()
  }
}

const onToggleStatus = async (row) => {
  const nextEnabled = !row.enabled
  try {
    await ElMessageBox.confirm(
      nextEnabled ? `确认解封用户「${row.username}」？` : `确认封禁用户「${row.username}」？封禁后该用户将无法登录。`,
      '请确认',
      { type: 'warning' }
    )
    await updateUserStatus(row.id, nextEnabled)
    row.enabled = nextEnabled
    ElMessage.success(nextEnabled ? '已解封' : '已封禁')
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const onDeleteUser = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除用户「${row.username}」？此操作不可恢复。`, '请确认', { type: 'warning' })
    await deleteUser(row.id)
    ElMessage.success('已删除')
    loadUsers()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

// ---------- 单词库管理 ----------
const words = ref([])
const wordsLoading = ref(false)
const wordSearchKeyword = ref('')

const filteredWords = computed(() => {
  if (!wordSearchKeyword.value) return words.value
  const kw = wordSearchKeyword.value.toLowerCase()
  return words.value.filter(
    (w) => w.word?.toLowerCase().includes(kw) || w.definition?.includes(wordSearchKeyword.value)
  )
})

const loadWords = async () => {
  wordsLoading.value = true
  try {
    words.value = await getAdminWords()
  } catch (e) {
    console.error(e)
  } finally {
    wordsLoading.value = false
  }
}

const difficultyLabel = (level) => ({ EASY: '简单', MEDIUM: '中等', HARD: '困难' }[level] || level)
const difficultyClass = (level) => ({ EASY: 'ok', MEDIUM: 'warn', HARD: 'banned' }[level] || '')

const wordDialogVisible = ref(false)
const wordSaving = ref(false)
const editingWord = ref(null)
const wordForm = reactive({
  word: '',
  phonetic: '',
  definition: '',
  aiDefinition: '',
  partOfSpeech: '',
  difficultyLevel: 'MEDIUM',
  audioUrl: ''
})

const resetWordForm = () => {
  wordForm.word = ''
  wordForm.phonetic = ''
  wordForm.definition = ''
  wordForm.aiDefinition = ''
  wordForm.partOfSpeech = ''
  wordForm.difficultyLevel = 'MEDIUM'
  wordForm.audioUrl = ''
}

const openWordDialog = (row) => {
  if (row) {
    editingWord.value = row
    Object.assign(wordForm, {
      word: row.word,
      phonetic: row.phonetic,
      definition: row.definition,
      aiDefinition: row.aiDefinition,
      partOfSpeech: row.partOfSpeech,
      difficultyLevel: row.difficultyLevel,
      audioUrl: row.audioUrl
    })
  } else {
    editingWord.value = null
    resetWordForm()
  }
  wordDialogVisible.value = true
}

const saveWord = async () => {
  if (!wordForm.word || !wordForm.definition) {
    ElMessage.warning('单词和释义为必填项')
    return
  }
  wordSaving.value = true
  try {
    if (editingWord.value) {
      await apiUpdateWord(editingWord.value.id, wordForm)
      ElMessage.success('已更新')
    } else {
      await apiCreateWord(wordForm)
      ElMessage.success('已添加')
    }
    wordDialogVisible.value = false
    loadWords()
  } catch (e) {
    console.error(e)
  } finally {
    wordSaving.value = false
  }
}

const onDeleteWord = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除单词「${row.word}」？关联的例句、学习记录也会一并删除。`, '请确认', { type: 'warning' })
    await apiDeleteWord(row.id)
    ElMessage.success('已删除')
    loadWords()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}
// ---------- 批量删除 ----------
const selectedWordIds = ref([])
const onWordSelectionChange = (rows) => {
  selectedWordIds.value = rows.map((r) => r.id)
}
const onBatchDeleteWords = async () => {
  try {
    await ElMessageBox.confirm(
      `确认删除选中的 ${selectedWordIds.value.length} 个单词？关联的例句、学习记录也会一并删除，此操作不可恢复。`,
      '请确认',
      { type: 'warning' }
    )
    await batchDeleteWords(selectedWordIds.value)
    ElMessage.success('已删除')
    selectedWordIds.value = []
    loadWords()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}


// ---------- 例句管理 ----------
const exampleDialogVisible = ref(false)
const exampleTargetWord = ref(null)
const examples = ref([])
const exampleSaving = ref(false)
const exampleForm = reactive({ sentence: '', translation: '' })

const openExampleDialog = async (row) => {
  exampleTargetWord.value = row
  exampleForm.sentence = ''
  exampleForm.translation = ''
  exampleDialogVisible.value = true
  try {
    examples.value = await getExampleSentences(row.id)
  } catch (e) {
    console.error(e)
  }
}

const addExample = async () => {
  if (!exampleForm.sentence) {
    ElMessage.warning('请填写英文例句')
    return
  }
  exampleSaving.value = true
  try {
    await addExampleSentence(exampleTargetWord.value.id, exampleForm)
    ElMessage.success('已添加')
    exampleForm.sentence = ''
    exampleForm.translation = ''
    examples.value = await getExampleSentences(exampleTargetWord.value.id)
  } catch (e) {
    console.error(e)
  } finally {
    exampleSaving.value = false
  }
}

const onDeleteExample = async (ex) => {
  try {
    await deleteExampleSentence(ex.id)
    examples.value = examples.value.filter((e) => e.id !== ex.id)
    ElMessage.success('已删除')
  } catch (e) {
    console.error(e)
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '—'
  return dateStr.replace('T', ' ').slice(0, 16)
}

// ---------- PDF / 文本导入 ----------
const importMode = ref('file')
const useAi = ref(false)
const importFile = ref(null)
const pasteText = ref('')
const parsing = ref(false)
const importItems = ref([])
const importing = ref(false)
const aiMeta = ref(null) // { truncated, totalChunks, processedChunks, failedChunks }，仅AI模式下有值

const selectedImportCount = computed(() => importItems.value.filter((i) => i.selected).length)

const onImportFileChange = (uploadFile) => {
  importFile.value = uploadFile.raw
  importItems.value = []
  aiMeta.value = null
}

const applyParsedItems = (items) => {
  importItems.value = items.map((item) => ({ ...item, selected: !item.alreadyExists }))
  if (!items.length) {
    ElMessage.warning('没有解析出可识别的单词条目')
  } else {
    ElMessage.success(`解析出 ${items.length} 条候选单词`)
  }
}

const parseImportFile = async () => {
  if (!importFile.value) return
  parsing.value = true
  aiMeta.value = null
  try {
    if (useAi.value) {
      const result = await parseAiFileImport(importFile.value)
      aiMeta.value = {
        truncated: result.truncated,
        totalChunks: result.totalChunks,
        processedChunks: result.processedChunks,
        failedChunks: result.failedChunks
      }
      applyParsedItems(result.items)
    } else {
      const result = await parsePdfImport(importFile.value)
      applyParsedItems(result)
    }
  } catch (e) {
    console.error(e)
  } finally {
    parsing.value = false
  }
}

const parsePastedText = async () => {
  if (!pasteText.value.trim()) return
  parsing.value = true
  aiMeta.value = null
  try {
    if (useAi.value) {
      const result = await parseAiTextImport(pasteText.value)
      aiMeta.value = {
        truncated: result.truncated,
        totalChunks: result.totalChunks,
        processedChunks: result.processedChunks,
        failedChunks: result.failedChunks
      }
      applyParsedItems(result.items)
    } else {
      const result = await parseTextImport(pasteText.value)
      applyParsedItems(result)
    }
  } catch (e) {
    console.error(e)
  } finally {
    parsing.value = false
  }
}

const confirmImportSelected = async () => {
  const selected = importItems.value.filter((i) => i.selected)
  if (!selected.length) return
  importing.value = true
  try {
    const result = await confirmPdfImport(selected)
    ElMessage.success(`成功导入 ${result.imported} 个单词，跳过 ${result.skipped} 个重复单词`)
    importItems.value = []
    importFile.value = null
    pasteText.value = ''
    aiMeta.value = null
    loadWords()
  } catch (e) {
    console.error(e)
  } finally {
    importing.value = false
  }
}

onMounted(() => {
  getSystemStatistics().then((data) => (stats.value = data)).catch(console.error)
  loadUsers()
  loadWords()
})
</script>

<style scoped>
.admin-page {
  min-height: 100vh;
  background: var(--color-paper);
}

.top-bar {
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border-soft);
  position: sticky;
  top: 0;
  z-index: 10;
}
.top-bar-inner {
  max-width: 1240px;
  margin: 0 auto;
  padding: 14px var(--space-6);
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.brand {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  cursor: pointer;
}
.brand-mark {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  background: var(--color-primary-deep);
  display: flex;
  align-items: center;
  justify-content: center;
}
.brand-mark span {
  font-family: var(--font-display);
  font-weight: 700;
  color: #fff;
  font-size: 16px;
}
.brand-name {
  font-size: 18px;
  font-weight: 700;
}
.user-zone {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}
.user-greeting {
  font-size: 14px;
  color: var(--color-ink-soft);
}
.ghost-btn {
  border: 1px solid var(--color-border);
  background: transparent;
  color: var(--color-ink-soft);
  padding: 7px 16px;
  border-radius: var(--radius-sm);
  font-size: 13px;
  cursor: pointer;
}
.ghost-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.main-content {
  max-width: 1240px;
  margin: 0 auto;
  padding: var(--space-6);
}

.stat-row {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: var(--space-4);
  margin-bottom: var(--space-6);
}
.mini-stat {
  background: var(--color-surface);
  border: 1px solid var(--color-border-soft);
  border-radius: var(--radius-sm);
  padding: var(--space-4);
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.mini-stat-label {
  font-size: 12px;
  color: var(--color-ink-soft);
}
.mini-stat-value {
  font-family: var(--font-mono);
  font-size: 22px;
  font-weight: 500;
}

.tab-strip {
  display: flex;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
  border-bottom: 1px solid var(--color-border-soft);
}
.tab-btn {
  border: none;
  background: transparent;
  padding: 10px 4px;
  margin-right: var(--space-5);
  font-size: 14px;
  font-weight: 500;
  color: var(--color-ink-soft);
  cursor: pointer;
  border-bottom: 2px solid transparent;
}
.tab-btn.active {
  color: var(--color-primary);
  border-bottom-color: var(--color-primary);
}

.panel {
  background: var(--color-surface);
  border: 1px solid var(--color-border-soft);
  border-radius: var(--radius-card);
  padding: var(--space-5);
  box-shadow: var(--shadow-card);
}
.panel-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
}

.primary-btn.small {
  height: 34px;
  padding: 0 16px;
  border: none;
  border-radius: var(--radius-sm);
  background: var(--color-primary);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}
.primary-btn.small:hover:not(:disabled) {
  background: var(--color-primary-deep);
}
.primary-btn.small:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.status-pill {
  display: inline-block;
  font-size: 12px;
  padding: 2px 10px;
  border-radius: 999px;
}
.status-pill.ok {
  background: var(--color-moss-tint);
  color: var(--color-moss);
}
.status-pill.warn {
  background: var(--color-amber-tint);
  color: var(--color-amber);
}
.status-pill.banned {
  background: var(--color-rust-tint);
  color: var(--color-rust);
}

.example-list {
  max-height: 260px;
  overflow-y: auto;
  margin-bottom: var(--space-4);
}
.empty-hint {
  color: var(--color-ink-soft);
  font-size: 13px;
  text-align: center;
  padding: var(--space-5) 0;
}
.example-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-3);
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--color-border-soft);
}
.example-text .en {
  margin: 0 0 4px 0;
  font-size: 14px;
  color: var(--color-ink);
}
.example-text .zh {
  margin: 0;
  font-size: 13px;
  color: var(--color-ink-soft);
}
.example-form {
  border-top: 1px solid var(--color-border-soft);
  padding-top: var(--space-4);
}

.import-mode-switch {
  display: inline-flex;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  overflow: hidden;
  margin-bottom: var(--space-4);
}
.mode-btn {
  border: none;
  background: var(--color-surface);
  color: var(--color-ink-soft);
  padding: 7px 18px;
  font-size: 13px;
  cursor: pointer;
}
.mode-btn.active {
  background: var(--color-primary);
  color: #fff;
}
.import-paste {
  margin-bottom: var(--space-3);
}
.import-paste .primary-btn.small {
  margin-top: var(--space-3);
}
.import-upload {
  display: flex;
  align-items: flex-start;
  gap: var(--space-4);
  margin-bottom: var(--space-3);
}
.import-upload :deep(.el-upload-dragger) {
  width: 420px;
  padding: var(--space-4);
}
.upload-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  color: var(--color-ink-soft);
  font-size: 13px;
}
.import-hint {
  font-size: 12px;
  color: var(--color-ink-soft);
  margin: 0 0 var(--space-4) 0;
}
.ai-toggle {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}
.ai-toggle-label {
  font-size: 13px;
  color: var(--color-ink);
}
.ai-toggle-hint {
  font-size: 12px;
  color: var(--color-ink-soft);
}
.ai-truncated-hint {
  background: var(--color-amber-tint);
  color: var(--color-amber);
  font-size: 12px;
  padding: 8px 12px;
  border-radius: var(--radius-sm);
  margin-bottom: var(--space-3);
}
.import-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-3);
  font-size: 13px;
  color: var(--color-ink-soft);
}
</style>
<style>
.admin-page{min-height:100vh;background:#f4f1e8;background-image:radial-gradient(circle at 100% 0%,#dff3bc 0 14%,transparent 14.4%)}.top-bar{margin:18px auto 0;width:calc(100% - 36px);max-width:1320px;border:0;border-radius:18px;background:#0d2617;color:#fff}.top-bar-inner{padding:16px 28px}.brand-mark{background:#c6f19d;border-radius:50%}.brand-mark span{color:#102318}.brand-name{color:#fff}.user-greeting{color:#d6e4cf}.ghost-btn{border-radius:999px;border-color:rgba(198,241,157,.55);color:#c6f19d}.main-content{max-width:1280px;padding-top:42px}.mini-stat,.panel{border-color:#d6ddc9;border-radius:20px;background:#fffdf7;box-shadow:0 12px 30px rgba(24,47,30,.08)}.mini-stat:first-child{background:#0d2617}.mini-stat:first-child .mini-stat-label,.mini-stat:first-child .mini-stat-value{color:#fff}.tab-strip{gap:12px;border:0}.tab-btn{padding:9px 16px;margin:0;border-radius:999px;background:#e8ecdf}.tab-btn.active{color:#102318;border:0;background:#c6f19d}.primary-btn.small{border-radius:999px;background:#0d2617}.panel-toolbar{gap:16px}.status-pill{border-radius:999px}
</style>