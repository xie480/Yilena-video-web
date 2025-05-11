<template>
    <div class="scroll-container">
      <!-- 左箭头 -->
      <button 
        v-show="showLeftArrow"
        class="arrow left" 
        @click="scroll(-300)">
        ◀
      </button>
      
      <!-- 滚动区域 -->
      <div 
        ref="scrollWrapper"
        class="scroll-wrapper"
        @scroll="handleScroll">
        <div class="user-list">
          <!-- 全部项 -->
          <div 
            class="user-item"
            :class="{ active: activeIndex === 0 }"
            @click="handleSelect(0)">
            <div class="avatar-wrapper1">
              <div class="all-avatar1">
                <svg t="1743584588096" class="userScorll-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="35622" width="200" height="200"><path d="M545.04687527 520.78906277h-26.3671875c-116.36718777 0-210.9375-94.57031223-210.9375-210.9375s94.57031223-210.9375 210.9375-210.9375h26.3671875v421.875z m-52.734375-366.85546902c-74.70703152 12.65625-131.8359375 77.69531223-131.8359375 156.09375s57.12890598 143.4375 131.8359375 156.09375V153.93359375z" p-id="35623"></path><path d="M345.00781223 716.25781223c-116.36718777 0-210.9375-94.57031223-210.9375-210.9375v-26.3671875h421.875v26.3671875c0 116.19140598-94.57031223 210.9375-210.9375 210.9375z m-155.91796821-184.5703125c12.65625 74.70703152 77.69531223 131.8359375 156.09375 131.8359375s143.4375-57.12890598 156.09375-131.8359375H189.08984402z"  p-id="35624"></path><path d="M540.4765625 889.92968777h-26.3671875v-421.875h26.3671875c116.36718777 0 210.9375 94.57031223 210.9375 210.9375s-94.74609402 210.9375-210.9375 210.9375z m26.3671875-366.85546902v312.01171902c74.70703152-12.65625 131.8359375-77.69531223 131.8359375-156.09375s-57.12890598-143.4375-131.8359375-155.91796902z"  p-id="35625"></path><path d="M925.08593723 509.890625h-421.875v-26.3671875c0-116.36718777 94.57031223-210.9375 210.9375-210.9375s210.9375 94.57031223 210.9375 210.9375v26.3671875z m-366.85546821-52.734375h312.01171821c-12.65625-74.70703152-77.69531223-131.8359375-156.09375-131.8359375s-143.4375 57.12890598-155.91796821 131.8359375z" p-id="35626"></path></svg>
              </div>
            </div>
            <span class="username1">全部</span>
          </div>
  
          <!-- 用户列表 -->
          <div
            v-for="(user, index) in users"
            :key="user.id"
            class="user-item"
            :class="{ active: activeIndex === index + 1 }"
            @click="handleSelect(index + 1)">
            <div class="avatar-wrapper1">
                <el-avatar 
                :src= "user.image" 
                :size="47" 
                class="all-avatar1" 
                @mouseenter="hoverState = index"
                @mouseleave="hoverState = -1"></el-avatar>
            </div>
            <span class="username1">{{ user.username }}</span>
          </div>
        </div>
      </div>
  
      <!-- 右箭头 -->
      <button 
        v-show="showRightArrow"
        class="arrow right" 
        @click="scroll(300)">
        ▶
      </button>
    </div>
  </template>
  
  <script setup>
  import { ref, computed, onMounted } from 'vue'
  
  const props = defineProps({
    users: {
      type: Array,
      required: true
    }
  })
  
  const emit = defineEmits(['select'])
  
  // 响应式状态
  const scrollWrapper = ref(null)
  const activeIndex = ref(0)
  const hoverState = ref(-1)
  const showLeftArrow = ref(false)
  const showRightArrow = ref(true)
  
  // 计算用户总数（包含"全部"项）
  const totalItems = computed(() => props.users.length + 1)
  
  // 滚动处理
  const handleScroll = () => {
    const { scrollLeft, scrollWidth, clientWidth } = scrollWrapper.value
    showLeftArrow.value = scrollLeft > 0
    showRightArrow.value = scrollLeft + clientWidth < scrollWidth
  }
  
  const scroll = (offset) => {
    scrollWrapper.value.scrollBy({
      left: offset,
      behavior: 'smooth'
    })
  }
  
  // 选择处理
  const handleSelect = (index) => {
    activeIndex.value = index
    emit('select', index)
  }
  
  // 初始化容器宽度检测
  onMounted(() => {
    const observer = new ResizeObserver(entries => {
      showRightArrow.value = entries[0].target.scrollWidth > entries[0].target.clientWidth
    })
    observer.observe(scrollWrapper.value)
    handleSelect(0);
  })
  </script>
  
  <style scoped>
.userScorll-icon{
    width: 30px;
    height: 30px;
    fill:rgb(45, 188, 239)
}

  .scroll-container {
    position: relative;
    padding: 0 40px;
  }
  
  .scroll-wrapper {
    overflow-x: auto;
    scroll-behavior: smooth;
    -ms-overflow-style: none;
    scrollbar-width: none;
  }
  
  .scroll-wrapper::-webkit-scrollbar {
    display: none;
  }
  
  .user-list {
    display: flex;
    gap: 1.5rem;
    padding: 1rem 0;
  }
  
  .user-item {
    display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center; /* 垂直居中 */
  height: 100%;
  }
  
  .avatar-wrapper1 {
    position: relative;
    width: 60px;
    height: 60px;
    margin-bottom: 8px;
    flex-direction: column;
  }
  
  .all-avatar1 {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    background: rgb(223, 246, 253);
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: bold;
    margin-top: 20px;
    margin-left: 5px;
  }
  
  .user-item:hover .all-avatar1,
  .user-item.active .all-avatar1 {
    border-color: #2196F3;
  }
  
  .user-item:hover .username1,
  .user-item.active .username1 {
    color: #2196F3;
  }

  .all-avatar:hover{
    border-color: #2196F3;
  }

  .username1{
    font-size: 13px;
    margin-top: 8px; /* 控制文字与头像间距 */
  text-align: center;
  width: auto;
  /* 移除原有定位属性 */
  position: static;
  top: auto;
  margin-left: 0;
  color: rgb(135, 141, 145);
  }
  
  .arrow {
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background: rgba(255,255,255,0.9);
    border: 1px solid #ddd;
    cursor: pointer;
    z-index: 2;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  }
  
  .arrow:hover {
    background: #fff;
  }
  
  .left { left: 10px; }
  .right { right: 10px; }
  </style>