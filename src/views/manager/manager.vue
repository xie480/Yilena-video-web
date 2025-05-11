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
import { getUserList,updateUserStatus } from '@/api/user'
import { getManagerList,updateManagerStatus } from '@/api/manager'

const isLoading = ref(true)

const value = ref('')

watch(() => value.value, (newValue, oldValue) => {
   getUserPendingList(); 
})

const options = [
  {
    value: '3',
    label: '全部',
  },
  {
    value: '0',
    label: '冻结',
  },
  {
    value: '1',
    label: '正常',
  }
]

const tableData = ref([])

const userQueryEntity = ref({
    username: '',
    status: '',
    beginTime: '',
    endTime: '',
    page: 1,
    pageSize: 10
})

const input = ref('')

const getUserPendingList = async () => {
  userQueryEntity.value.username = input.value;
  userQueryEntity.value.status = value.value;
  if(userQueryEntity.value.status == '3'){
    userQueryEntity.value.status = '';
  }
  const result = await getManagerList(userQueryEntity.value.username,userQueryEntity.value.beginTime,userQueryEntity.value.endTime,userQueryEntity.value.status,userQueryEntity.value.page,userQueryEntity.value.pageSize)
  if(result.code === 1){
    tableData.value = result.data.rows;
    tableData.value.forEach((item) => {
      item.createdTime = formatDate(item.createdTime);
      item.updatedTime = formatDate(item.updatedTime); 
    })
    total.value = result.data.total;
    console.log(result.data);
  }
}

// 分页相关数据
const currentPage = ref(1) // 当前页码
const pageSize = ref(10) // 每页显示条数
const total = ref(0) // 总数据量

// 分页方法
const handleSizeChange = async(val: number) => {
  pageSize.value = val
  currentPage.value = 1 ;
  userQueryEntity.value.page = currentPage.value;
  userQueryEntity.value.pageSize = pageSize.value;
  getUserPendingList();
}

const handleCurrentChange = async(val: number) => {
  currentPage.value = val
  console.log(currentPage.value)
  userQueryEntity.value.page = currentPage.value;
  getUserPendingList();
}

const value2 = ref()

watch(() => value2.value, (newValue, oldValue) => {
   if (!newValue) {
    userQueryEntity.value.beginTime = '';
    userQueryEntity.value.endTime = '';
  getUserPendingList();
    return;
  }
  console.log(newValue)
   userQueryEntity.value.beginTime = new Date(newValue[0]).toISOString().slice(0, 19);
  userQueryEntity.value.endTime = new Date(newValue[1]).toISOString().slice(0, 19);
  getUserPendingList();
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

const updateEntity = ref({
  id: '',
  status: '',
  reason: ''
})

const updateCurrentUserStatus = async (id, status) => {
  console.log(id,status)
  if(status == 0){
    updateEntity.value.status = '1';
    ElMessageBox.confirm('确定要恢复该管理员吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      const res = await updateManagerStatus(id,updateEntity.value.status);
      if(res.code === 1){
        ElMessage.success('恢复成功');
        getUserPendingList(); 
      }
    })
  }else{
    updateEntity.value.status = '0';
    ElMessageBox.confirm('确定要禁用该管理员吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      const res = await updateManagerStatus(id,updateEntity.value.status);
      if(res.code === 1){
        ElMessage.success('禁用成功');
        getUserPendingList(); 
      }
    })
  }
};

const formatDate = (dateString) => {
  return dateString.replace('T', ' ');
}

const tableRowClassName = ({
  row,
  rowIndex,
}: {
  row: User
}) => {
  if(row.status === 0){
    return 'danger-row' 
  }
  return ''
}

onMounted(async() => {
    await getUserPendingList();
    isLoading.value = false
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
 <div class="user-manage" v-else>
    <el-row>
    <!-- From Uiverse.io by satyamchaudharydev --> 
  <form class="form" @submit.native.prevent>
      <button @click="getUserPendingList">
          <svg width="17" height="16" fill="none" xmlns="http://www.w3.org/2000/svg" role="img" aria-labelledby="search">
              <path d="M7.667 12.667A5.333 5.333 0 107.667 2a5.333 5.333 0 000 10.667zM14.334 14l-2.9-2.9" stroke="currentColor" stroke-width="1.333" stroke-linecap="round" stroke-linejoin="round"></path>
          </svg>
      </button>
      <input 
      class="input" 
      v-model="input" 
      placeholder="请输入用户名" 
      type="text"
      @keyup.enter="getUserPendingList"
      >
      <button class="reset" type="reset">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12"></path>
          </svg>
      </button>
  </form>
  <div class="user-datePicker">
    <el-date-picker
      v-model="value2"
      type="datetimerange"
      :shortcuts="shortcuts"
      range-separator="到"
      start-placeholder="开始时间(入职时间)"
      end-placeholder="结束时间"
      class="user-datePicker"
    />
    </div>
  <el-select
      v-model="value"
      placeholder="全部"
      size="large"
      style="width: 240px"
      class="user-select"
    >
      <el-option
        v-for="item in options"
        :key="item.value"
        :label="item.label"
        :value="item.value"
      />
    </el-select>
    </el-row>
    <el-table 
    :data="tableData" 
    class="user-table"
    :row-class-name="tableRowClassName"
    >
    <el-table-column align="center" fixed prop="id" label="ID" width="150" />
    <el-table-column align="center" fixed prop="username" label="用户名" width="200" />
    <el-table-column align="center" prop="status" label="状态" width="200">
      <template #default="scope">
        <el-tag :type="scope.row.status === 0 ? 'danger' : 'success'">
          {{ scope.row.status === 0 ? '冻结' : '正常' }}
        </el-tag>
      </template>
    </el-table-column>
    <el-table-column align="center" fixed="right" prop="createdTime" label="入职时间" width="250" />
    <el-table-column align="center" prop="updatedTime" label="修改时间" width="250" />
    <el-table-column align="center" fixed="right" label="操作" min-width="100">
      <template #default="scope">
        <el-button @click="updateCurrentUserStatus(scope.row.id,scope.row.status)" link :type="scope.row.status === 0 ? 'primary' : 'danger'" size="small">
          {{ scope.row.status === 0 ? '恢复' : '禁用' }}
        </el-button>
      </template>
    </el-table-column>
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
</template>

<style scoped>
.el-table .danger-row {
  --el-table-tr-bg-color: var(--el-color-danger-light-9) !important;
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

.user-datePicker{
    margin-top: 7px;
    margin-left: 203px;
}

.user-table{
    margin-top: 15px;
    width: 95% !important;
    justify-self: center;
}

.user-select{
    margin-top: 10px;
    margin-left: 220px;
}

.form button {
  border: none;
  background: none;
  color: #8b8ba7;
}
/* styling of whole input container */
.form {
  --timing: 0.3s;
  --width-of-input: 200px;
  --height-of-input: 40px;
  --border-height: 2px;
  --input-bg: #fff;
  --border-color: rgb(152, 219, 253);
  --border-radius: 30px;
  --after-border-radius: 1px;
  position: relative;
  width: 220px;
  height: var(--height-of-input);
  display: flex;
  align-items: center;
  padding-inline: 0.8em;
  border-radius: var(--border-radius);
  transition: border-radius 0.5s ease;
  background: var(--input-bg,#fff);
  margin-top: 10px;
  margin-left: 40px;
}
/* styling of Input */
.input {
  font-size: 0.9rem;
  background-color: transparent;
  width: 100%;
  height: 100%;
  padding-inline: 0.5em;
  padding-block: 0.7em;
  border: none;
}
/* styling of animated border */
.form:before {
  content: "";
  position: absolute;
  background: var(--border-color);
  transform: scaleX(0);
  transform-origin: center;
  width: 100%;
  height: var(--border-height);
  left: 0;
  bottom: 0;
  border-radius: 1px;
  transition: transform var(--timing) ease;
}
/* Hover on Input */
.form:focus-within {
  border-radius: var(--after-border-radius);
}

input:focus {
  outline: none;
}
/* here is code of animated border */
.form:focus-within:before {
  transform: scale(1);
}
/* styling of close button */
/* == you can click the close button to remove text == */
.reset {
  border: none;
  background: none;
  opacity: 0;
  visibility: hidden;
}
/* close button shown when typing */
input:not(:placeholder-shown) ~ .reset {
  opacity: 1;
  visibility: visible;
}
/* sizing svg icons */
.form svg {
  width: 17px;
  margin-top: 3px;
}

.user-manage {
  background-color: rgb(240, 248, 255);
  width: 1400px;
  height: calc(100vh - 100px); /* 动态高度（视窗高度 - 顶部间距） */
  margin-top: 20px;
  overflow-y: auto; /* 垂直滚动 */
  position: relative;
  justify-self: center;
}

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
</style>
