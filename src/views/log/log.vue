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
import { getLogList } from '@/api/log'

const activeName = ref('user')

const isLoading = ref(true)

const value2 = ref()

const queryEntity = ref({
  beginTime: '',
  endTime: '',
  page: 1,
  pageSize: 10,
  type: ''
})

const getCurrentLog = async () => {
  if(currentTag.value == 'user'){
    queryEntity.value.type = '0';
  }else{
    queryEntity.value.type = '1';
  }
  const res = await getLogList(queryEntity.value.beginTime,queryEntity.value.endTime,queryEntity.value.page,queryEntity.value.pageSize,queryEntity.value.type);
  if(res.code == 1){
    if(currentTag.value == 'user'){
    userLog.value = res.data.rows;
    userLogTotal.value = res.data.total
  }else{
    managerLog.value = res.data.rows;
    managerLogTotal.value = res.data.total
  }
  }
}

const userLog = ref([])
const userLogTotal = ref(0)
const managerLog = ref([])
const managerLogTotal = ref(0)


watch(() => value2.value, (newValue, oldValue) => {
   if (!newValue) {
    queryEntity.value.beginTime = '';
    queryEntity.value.endTime = '';
  getCurrentLog();
    return;
  }
  console.log(newValue)
   queryEntity.value.beginTime = new Date(newValue[0]).toISOString().slice(0, 19);
  queryEntity.value.endTime = new Date(newValue[1]).toISOString().slice(0, 19);
  getCurrentLog();
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

// 分页相关数据
const currentPage = ref(1) // 当前页码
const pageSize = ref(10) // 每页显示条数
const total = ref(0) // 总数据量
const currentTag = ref('') // 状态标签
const handleClick = (tab: TabsPaneContext, event: Event) => {
  console.log(tab, event)
  pageSize.value = 10;
  currentPage.value = 1;
  value2.value = '';
 if (tab.props.name === 'user') {
    total.value = userLogTotal.value;
    currentTag.value = 'user';
  } else if (tab.props.name === 'manager') {
    total.value = managerLogTotal.value;
    currentTag.value = 'manager';
  }
}

// 分页方法
const handleSizeChange = async(val: number) => {
  pageSize.value = val
  currentPage.value = 1 ;
  queryEntity.value.page = currentPage.value;
  queryEntity.value.pageSize = pageSize.value;
  getCurrentLog();
}

const handleCurrentChange = async(val: number) => {
  currentPage.value = val
  console.log(currentPage.value)
  queryEntity.value.page = currentPage.value;
  getCurrentLog();
}

onMounted(async() => {
  currentTag.value = 'user';
  await getCurrentLog();
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
 <div class="log-manage" v-else>
  <div class="user-datePicker">
    <el-date-picker
      v-model="value2"
      type="datetimerange"
      :shortcuts="shortcuts"
      range-separator="到"
      start-placeholder="开始时间(操作时间)"
      end-placeholder="结束时间"
      class="user-datePicker"
    />
    </div>
     <el-tabs class="log-tabs" v-model="activeName" @tab-click="handleClick">
    <el-tab-pane label="用户日志" name="user">
      <el-table 
    :data="userLog" 
    class="user-table"
    >
    <el-table-column align="center" prop="id" label="ID" width="150" />
    <el-table-column align="center" prop="operateUserId" label="操作用户ID" width="150" />
    <el-table-column align="center" prop="className" label="操作类型" width="130" />
    <el-table-column align="center" prop="methodName" label="调用接口名称" width="150" />
    <el-table-column align="center" show-overflow-tooltip prop="methodParams" label="传入参数列表" width="250" />
    <el-table-column align="center" show-overflow-tooltip prop="returnValue" label="返回参数列表" width="250" />
    <el-table-column align="center" prop="costTime" label="耗时(s)" width="70" />
    <el-table-column align="center" prop="operateTime" label="操作时间"/>
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
    </el-tab-pane>
    <el-tab-pane label="管理日志" name="manager">
      <el-table 
    :data="managerLog" 
    class="user-table"
    >
    <el-table-column align="center" prop="id" label="ID" width="150" />
    <el-table-column align="center" prop="operateUserId" label="操作管理员ID" width="150" />
    <el-table-column align="center" prop="className" label="操作类型" width="130" />
    <el-table-column align="center" prop="methodName" label="调用接口名称" width="150" />
    <el-table-column align="center" show-overflow-tooltip prop="methodParams" label="传入参数列表" width="250" />
    <el-table-column align="center" show-overflow-tooltip prop="returnValue" label="返回参数列表" width="250" />
    <el-table-column align="center" prop="costTime" label="耗时(s)" width="70" />
    <el-table-column align="center" prop="operateTime" label="操作时间"/>
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
    </el-tab-pane>
  </el-tabs>
 </div>
</template>

<style scoped>
.user-datePicker{
  position: absolute;
  top: -7px;
  right: 10px;
  z-index: 10;
}

.log-tabs{
  width: 1390px;
  margin-left: 10px;
}

.log-tabs > .el-tabs__content {
  padding: 32px;
  color: #6b778c;
  font-size: 32px;
  font-weight: 600;
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

.log-manage {
  background-color: rgb(240, 248, 255);
  width: 1400px;
  height: calc(100vh - 100px); /* 动态高度（视窗高度 - 顶部间距） */
  margin-top: 20px;
  overflow-y: auto; /* 垂直滚动 */
  position: relative;
  justify-self: center;
}
</style>
