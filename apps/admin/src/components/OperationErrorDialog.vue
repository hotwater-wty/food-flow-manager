<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useOperationFeedbackStore } from '../stores/operation-feedback'

const feedback = useOperationFeedbackStore()
const { detail, detailVisible } = storeToRefs(feedback)
</script>

<template>
  <el-dialog v-model="detailVisible" title="操作错误详情" width="500px" append-to-body>
    <el-descriptions v-if="detail" :column="1" border>
      <el-descriptions-item label="错误码">{{ detail.detail.errorCode }}</el-descriptions-item>
      <el-descriptions-item label="错误类型">{{ detail.detail.kind }}</el-descriptions-item>
      <el-descriptions-item label="HTTP 状态">{{ detail.detail.httpStatus ?? '—' }}</el-descriptions-item>
      <el-descriptions-item label="请求"
        >{{ detail.detail.request?.method ?? '—' }} {{ detail.detail.request?.url ?? '' }}</el-descriptions-item
      >
      <el-descriptions-item label="错误信息">{{ detail.detail.message }}</el-descriptions-item>
      <el-descriptions-item label="发生时间">{{
        new Date(detail.detail.occurredAt).toLocaleString('zh-CN', { hour12: false })
      }}</el-descriptions-item>
    </el-descriptions>
  </el-dialog>
</template>
