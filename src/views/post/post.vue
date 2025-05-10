<script lang="ts" setup>
import type { TabsPaneContext } from 'element-plus'
import { ref, reactive, onMounted,computed,watch } from 'vue'
import { useRouter } from 'vue-router'
import { useTransition } from '@vueuse/core'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Iphone,
  Location,
  OfficeBuilding,
  Tickets,
  User,Search 
} from '@element-plus/icons-vue'
import type { ComponentSize } from 'element-plus'
// 视频播放组件
import "vue3-video-play/dist/style.css";
	import videoPlay from "vue3-video-play";
import { getPostList,updatePostStatus } from '@/api/post'
// 分页相关数据
const currentPage = ref(1) // 当前页码
const pageSize = ref(10) // 每页显示条数
const total = ref(0) // 总数据量

const post = ref([]);

const postQueryEntity = ref({
  beginTime: '', // 开始时间
  endTime: '', // 结束时间
  page: 1, // 当前页码
  pageSize: 10, // 每页显示条数
})

const expandedRows = ref([]) // 存储当前展开的行ID

// 处理展开变化
const handleExpandChange = (row) => {
  if (expandedRows.value.includes(row.id)) {
    // 如果点击的是已展开的行，则关闭
    expandedRows.value = []
  } else {
    // 否则展开新行，关闭其他所有行
    expandedRows.value = [row.id]
  }
}

const getCurrentPostList = async () => {
	const result = await getPostList(postQueryEntity.value.beginTime,postQueryEntity.value.endTime,postQueryEntity.value.page,postQueryEntity.value.pageSize);
    if(result.code == 1){
        post.value = result.data.rows;
        post.value.forEach(item => {
            item.createdTime = formatDate(item.createdTime);
            item.updatedTime = formatDate(item.updatedTime);
        })
        total.value = result.data.total;
        console.log(post.value);
    }
}

const givePostNoPass1 = (id) => {
    dialogTableVisible.value = true;
    passEntity.value.id = id;
    passEntity.value.reason = '';
}



// 分页方法
const handleSizeChange = async(val: number) => {
  pageSize.value = val
  currentPage.value = 1 ;
  postQueryEntity.value.page = currentPage.value;
  postQueryEntity.value.pageSize = pageSize.value;
  getCurrentPostList();
}

const handleCurrentChange = async(val: number) => {
  currentPage.value = val
  console.log(currentPage.value)
  postQueryEntity.value.page = currentPage.value;
  getCurrentPostList();
}

const value2 = ref('')

watch(value2, (newValue, oldValue) => {
 if (!newValue) {
    postQueryEntity.value.beginTime = '';
    postQueryEntity.value.endTime = '';
  getCurrentPostList();
    return;
  }
  console.log(newValue)
   postQueryEntity.value.beginTime = new Date(newValue[0]).toISOString().slice(0, 19);
  postQueryEntity.value.endTime = new Date(newValue[1]).toISOString().slice(0, 19);
  getCurrentPostList();
})

const shortcuts = [
  {
    text: '7天内',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setDate(start.getDate() - 7)
      return [start, end]
    },
  },
  {
    text: '1个月内',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setMonth(start.getMonth() - 1)
      return [start, end]
    },
  },
  {
    text: '3个月内',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setMonth(start.getMonth() - 3)
      return [start, end]
    },
  },
]

const size = ref<ComponentSize>('default')

const iconStyle = computed(() => {
  const marginMap = {
    large: '8px',
    default: '6px',
    small: '4px',
  }
  return {
    marginRight: marginMap[size.value] || marginMap.default,
  }
})
const blockMargin = computed(() => {
  const marginMap = {
    large: '32px',
    default: '28px',
    small: '24px',
  }
  return {
    marginTop: marginMap[size.value] || marginMap.default,
  }
})

const isLoading = ref(true)

const dialogTableVisible = ref(false)

const passEntity = ref({
    id: '',
    reason: ''
})

const givePostNoPass = async() => {
    if(passEntity.value.reason == ''){
        ElMessage({
            type: 'error',
            message: '请填写原因'
        })
        return;
    }
    const result = await updatePostStatus(passEntity.value);
    if(result.code == 1){
        dialogTableVisible.value = false;
        ElMessage({
            type: 'success',
            message: '操作成功'
        })	
        getCurrentPostList();
    }
}

const formatDate = (dateString) => {
  return dateString.replace('T', ' ');
}

// 初始化时获取数据
onMounted(async() => {
    await getCurrentPostList();
  isLoading.value = false;
})
</script>

<template>
<div class="loader" v-if="isLoading">
  <div class="wrapper">
    <div class="catContainer">
      <svg
        xmlns="http://www.w3.org/2000/svg"
        fill="none"
        viewBox="0 0 733 673"
        class="catbody"
      >
        <path
          fill="#212121"
          d="M111.002 139.5C270.502 -24.5001 471.503 2.4997 621.002 139.5C770.501 276.5 768.504 627.5 621.002 649.5C473.5 671.5 246 687.5 111.002 649.5C-23.9964 611.5 -48.4982 303.5 111.002 139.5Z"
        ></path>
        <path fill="#212121" d="M184 9L270.603 159H97.3975L184 9Z"></path>
        <path fill="#212121" d="M541 0L627.603 150H454.397L541 0Z"></path>
      </svg>
      <svg
        xmlns="http://www.w3.org/2000/svg"
        fill="none"
        viewBox="0 0 158 564"
        class="tail"
      >
        <path
          fill="#191919"
          d="M5.97602 76.066C-11.1099 41.6747 12.9018 0 51.3036 0V0C71.5336 0 89.8636 12.2558 97.2565 31.0866C173.697 225.792 180.478 345.852 97.0691 536.666C89.7636 553.378 73.0672 564 54.8273 564V564C16.9427 564 -5.4224 521.149 13.0712 488.085C90.2225 350.15 87.9612 241.089 5.97602 76.066Z"
        ></path>
      </svg>
      <div class="text">
        <span class="bigzzz">Z</span>
        <span class="zzz">Z</span>
      </div>
    </div>
    <div class="wallContainer">
      <svg
        xmlns="http://www.w3.org/2000/svg"
        fill="none"
        viewBox="0 0 500 126"
        class="wall"
      >
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="3"
          x2="450"
          y1="3"
          x1="50"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="85"
          x2="400"
          y1="85"
          x1="100"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="122"
          x2="375"
          y1="122"
          x1="125"
        ></line>
        <line stroke-width="6" stroke="#7C7C7C" y2="43" x2="500" y1="43"></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="1.99391"
          x2="115.5"
          y1="43.0061"
          x1="115.5"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="2.00002"
          x2="189"
          y1="43.0122"
          x1="189"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="2.00612"
          x2="262.5"
          y1="43.0183"
          x1="262.5"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="2.01222"
          x2="336"
          y1="43.0244"
          x1="336"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="2.01833"
          x2="409.5"
          y1="43.0305"
          x1="409.5"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="43"
          x2="153"
          y1="84.0122"
          x1="153"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="43"
          x2="228"
          y1="84.0122"
          x1="228"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="43"
          x2="303"
          y1="84.0122"
          x1="303"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="43"
          x2="378"
          y1="84.0122"
          x1="378"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="84"
          x2="192"
          y1="125.012"
          x1="192"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="84"
          x2="267"
          y1="125.012"
          x1="267"
        ></line>
        <line
          stroke-width="6"
          stroke="#7C7C7C"
          y2="84"
          x2="342"
          y1="125.012"
          x1="342"
        ></line>
      </svg>
    </div>
  </div>
</div>

  <div class="video-manage" v-else>
    <div class="post-datePicker">
    <el-date-picker
      v-model="value2"
      type="datetimerange"
      :shortcuts="shortcuts"
      range-separator="到"
      start-placeholder="开始时间(投稿时间)"
      end-placeholder="结束时间"
    />
    </div>
    <div
        v-if="post.length > 0">
        <el-table
    :data="post"
    class="post-table"
    row-key="id" 
    :expand-row-keys="expandedRows"  
    @expand-change="handleExpandChange"
  >
    <el-table-column type="expand">
      <template #default="props">
        <div m="4" class="video-expand">
            <el-descriptions
    class="margin-top"
    :column="2"
    :size="size"
    border
    label-width="150px"
  >
    <el-descriptions-item :width="400"
    align="center">
      <template #label>
        <div class="cell-item">
          <el-icon :style="iconStyle">
            <user />
          </el-icon>
          作者
        </div>
      </template>
      {{ props.row.username }}
    </el-descriptions-item>
    <el-descriptions-item
    align="center">
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><ArrowRight /></el-icon>
          作者ID
        </div>
      </template>
      {{ props.row.userId }}
    </el-descriptions-item>
    <el-descriptions-item
    align="center">
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><CollectionTag /></el-icon>
          标签
        </div>
      </template>
      <el-tag style="margin-left: 5px;" v-for="(item,index) in props.row.tags" size="small">{{ item }}</el-tag>
    </el-descriptions-item>
    <el-descriptions-item
    align="center">
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><Pointer /></el-icon>
          点赞量
        </div>
      </template>
      {{ props.row.likes  }}
    </el-descriptions-item>
    <el-descriptions-item
    align="center">
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><Share /></el-icon>
          转发量
        </div>
      </template>
      {{ props.row.shares }}
    </el-descriptions-item>
    <el-descriptions-item
    align="center">
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><ChatLineRound /></el-icon>
          评论数
        </div>
      </template>
      {{ props.row.comments }}
    </el-descriptions-item>
    <el-descriptions-item
    align="center">
      <template #label>
        <div class="cell-item">
            <el-icon :style="iconStyle"><Switch /></el-icon>
          可见性
        </div>
      </template>
      <el-tag :type="props.row.visibility == 1? 'success': 'danger'">{{ props.row.visibility == 1 ? '公开' : '私密' }}</el-tag>
    </el-descriptions-item>
  </el-descriptions>
  <h3 style="margin-top: 30px;color: rgb(48, 49, 51);">内容</h3>
  <el-row>
  <div class="post-content">
    {{ props.row.content }}
  </div>
</el-row>
<el-row>
    <img v-for="(item,index) in props.row.imageUrl" :key="index" :src="item" style="max-height: 200px;margin-right: 10px;">
</el-row>
  <div class="button-group1">
    <el-button v-if="props.row.visibility == 1" type="danger" plain size="large" @click="givePostNoPass1(props.row.id)">下架</el-button>
  </div>
        </div>
      </template>
    </el-table-column>
    <el-table-column align="center" label="ID" prop="id" width="150"/>
    <el-table-column align="center" label="动态标题" prop="title">
        <template #default="props">
            {{ (props.row.title == null || props.row.title == '') ? '无' : props.row.title }}
        </template>
        </el-table-column>
    <el-table-column align="center" label="投稿时间" prop="createdTime" />
    <el-table-column align="center" label="修改时间" prop="updatedTime" />
  </el-table>
  <el-pagination
  class="el-pagination"
  background
  :current-page="currentPage"
  :hide-on-single-page="total < 10"
  :page-sizes="[10, 20, 50, 100]"
  :page-size="pageSize"
  layout="total, sizes, prev, pager, next, jumper"
  :total="total"
  @size-change="handleSizeChange"
  @current-change="handleCurrentChange"
/>
</div>
  <div v-else>
    <el-empty description="description" />
  </div>
  </div>

   <el-dialog v-model="dialogTableVisible" title="下架动态" width="800">
    <el-form-item label="原因">
      <el-input v-model="passEntity.reason" />
    </el-form-item>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogTableVisible = false">取消</el-button>
        <el-button type="danger" @click="givePostNoPass">确认
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.post-table{
    margin-top: 50px;
    width: 95%;
    justify-self: center;
}

/* From Uiverse.io by vinodjangid07 */ 
.loader {
  width: fit-content;
  height: fit-content;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 200px;
  margin-left: 560px;
}
.wrapper {
  width: fit-content;
  height: fit-content;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}
.catContainer {
  width: 100%;
  height: fit-content;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.catbody {
  width: 80px;
}
.tail {
  position: absolute;
  width: 17px;
  top: 50%;
  animation: tail 0.5s ease-in infinite alternate-reverse;
  transform-origin: top;
}
@keyframes tail {
  0% {
    transform: rotateZ(60deg);
  }
  50% {
    transform: rotateZ(0deg);
  }
  100% {
    transform: rotateZ(-20deg);
  }
}
.wall {
  width: 300px;
}
.text {
  display: flex;
  flex-direction: column;
  width: 50px;
  position: absolute;
  margin: 0px 0px 100px 120px;
}
.zzz {
  color: black;
  font-weight: 700;
  font-size: 15px;
  animation: zzz 2s linear infinite;
}
.bigzzz {
  color: black;
  font-weight: 700;
  font-size: 25px;
  margin-left: 10px;
  animation: zzz 2.3s linear infinite;
}
@keyframes zzz {
  0% {
    color: transparent;
  }
  50% {
    color: black;
  }
  100% {
    color: transparent;
  }
}


.post-datePicker {
  text-align: center;
  border-right: solid 1px var(--el-border-color);
  flex: 1;
}
.post-datePicker:last-child {
  border-right: none;
}
.post-datePicker .demonstration {
  display: block;
  color: var(--el-text-color-secondary);
  font-size: 14px;
  margin-bottom: 20px;
}

.post-datePicker{
  position: absolute;
  margin-top: 7px;
  margin-left: 945px;
}

.video-search{
  position: absolute;
  right: 60px;
  margin-top: 7px;
}

.button-group1{
  margin-bottom: 20px;
  display:flex;
  justify-content:flex-end;
  margin-top: 30px;
}

.button-group{
  margin-bottom: 20px;
  display:flex;
  justify-content: space-between;
  margin-top: 30px;
}

.video-player{
  z-index: 10!important;
}

/* 视频播放器全屏模式下的 z-index */
:deep(.video-player.fullscreen) {
  z-index: 1000 !important; /* 确保全屏时最高 */
}

.el-pagination {
  margin-top: 20px;
  justify-content: center;
  margin-bottom: 20px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .el-pagination {
    flex-wrap: wrap;
  }
  .el-pagination__jump {
    margin-top: 10px;
  }
}

.el-descriptions {
  margin-top: 20px;
}
.cell-item {
  display: flex;
  align-items: center;
}
.margin-top {
  margin-top: 20px;
}

.video-expand-row3{
    margin-top: 20px;
}

.video-expand-row1{
    gap: 30px;
    margin-top: 20px;
    margin-left: 50px;
}

.video-expand-row2{
    gap: 30px;
    margin-bottom: 20px;
    margin-left: 50px;
}

.el-row{
  margin-bottom: 10px;
}

.video-expand{
    justify-self: center;
    width: 90%;
}

/* 外层容器固定高度，并启用滚动 */
.video-manage {
  background-color: rgb(240, 248, 255);
  width: 1400px;
  height: calc(100vh - 100px); /* 动态高度（视窗高度 - 顶部间距） */
  margin-top: 20px;
  overflow-y: auto; /* 垂直滚动 */
  position: relative;
  justify-self: center;
}

/* 表格宽度适配 */
.video-table {
  width: 1350px; /* 自动填充父容器宽度 */
  margin-bottom: 20px;
  justify-self: center;
}

.demo-tabs {
  align-items: center;
}

/* 标签页内容区域固定高度 */
.demo-tabs > .el-tabs__content {
  padding: 16px; /* 减少内边距 */
  color: #6b778c;
  min-height: calc(100% - 50px); /* 确保内容区域占满剩余空间 */
}

/* :deep(.el-tabs__header) {
  position: sticky;
  top: 0px;
  background-color: rgb(240, 248, 255); 
  z-index: 1;
} */

/* 调整 badge 样式 */
.badge-item {
  margin-left: 8px;
}

/* 自定义 badge 颜色 */
:deep(.el-badge__content.is-fixed) {
  &.el-badge__content--danger {
    background-color: #f56c6c;
  }
  &.el-badge__content--info {
    background-color: #909399;
  }
  &.el-badge__content--success {
    background-color: #67c23a;
  }
}
</style>