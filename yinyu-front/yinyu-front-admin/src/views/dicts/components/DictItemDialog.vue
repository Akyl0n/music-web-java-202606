<template>
  <el-dialog
    :model-value="modelValue"
    :title="dialogTitle"
    width="640px"
    class="song-dialog"
    destroy-on-close
    @close="handleClose"
  >
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="92px" class="song-edit-form">
      <div class="dialog-grid">
        <el-form-item label="所属类型">
          <el-input :model-value="typeDisplay" disabled />
        </el-form-item>
        <el-form-item label="字典值编码" prop="code">
          <el-input v-model="formData.code" placeholder="请输入字典值编码" />
        </el-form-item>
        <el-form-item label="字典值名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入字典值名称" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio-button label="enabled">启用</el-radio-button>
            <el-radio-button label="disabled">停用</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </div>
      <el-form-item label="备注">
        <el-input v-model="formData.remark" type="textarea" :rows="4" placeholder="请输入备注" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  mode: { type: String, default: 'create' },
  initialData: { type: Object, default: () => ({}) },
  activeType: { type: Object, default: null },
  submitting: { type: Boolean, default: false },
})

const emit = defineEmits(['update:modelValue', 'submit'])

const formRef = ref()
const formData = reactive(createDefaultForm())

const formRules = {
  code: [{ required: true, message: '请输入字典值编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入字典值名称', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

const dialogTitle = computed(() => (props.mode === 'create' ? '新增字典值' : '编辑字典值'))
const typeDisplay = computed(() => {
  if (!props.activeType) {
    return ''
  }
  return `${props.activeType.name} / ${props.activeType.code}`
})

watch(
  () => [props.modelValue, props.initialData],
  ([visible]) => {
    if (!visible) {
      return
    }
    Object.assign(formData, createDefaultForm(), props.initialData || {})
  },
  { deep: true, immediate: true },
)

function createDefaultForm() {
  return {
    id: undefined,
    typeId: undefined,
    code: '',
    name: '',
    status: 'enabled',
    remark: '',
  }
}

function handleClose() {
  emit('update:modelValue', false)
}

async function handleSubmit() {
  await formRef.value.validate()
  emit('submit', {
    ...formData,
    typeId: props.activeType?.id,
  })
}
</script>
