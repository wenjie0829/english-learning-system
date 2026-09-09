<template>
  <div ref="chartRef" class="base-chart" :style="{ height }"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  option: { type: Object, required: true },
  height: { type: String, default: '300px' }
})

const chartRef = ref(null)
let chart = null
let resizeObserver = null

const render = () => {
  if (!chart && chartRef.value) {
    chart = echarts.init(chartRef.value)
  }
  if (chart) {
    chart.setOption(props.option, true)
  }
}

onMounted(async () => {
  await nextTick()
  render()
  if (window.ResizeObserver) {
    resizeObserver = new ResizeObserver(() => chart && chart.resize())
    resizeObserver.observe(chartRef.value)
  }
})

watch(
  () => props.option,
  () => render(),
  { deep: true }
)

onBeforeUnmount(() => {
  if (resizeObserver) resizeObserver.disconnect()
  if (chart) {
    chart.dispose()
    chart = null
  }
})

defineExpose({ getInstance: () => chart })
</script>

<style scoped>
.base-chart {
  width: 100%;
}
</style>
