<template>
    <div class="follow-container">
      <div class="follow-wrapper">
        <button 
          class="follow-button"
          :class="{
            'following': isFollowing,
            'special': isSpecial
          }"
          @click="handleMainClick"
        >
          <span class="text">
            {{ isSpecial ? '已特别关注' : isFollowing ? '已关注' : '+ 未关注' }}
          </span>
          <span class="count">{{ fansCount }}</span>
        </button>
  
        <transition name="dropdown">
          <div v-if="showDropdown" class="dropdown-menu">
            <div class="dropdown-item" @click="setSpecial">
              <span>⭐ 特别关注</span>
            </div>
            <div class="dropdown-item danger" @click="unfollow">
              <span>❌ 取消关注</span>
            </div>
          </div>
        </transition>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref } from 'vue'
  
  const props = defineProps({
  initialFollowing: Boolean,
  initialSpecial: Boolean,
  initialCount: Number
});

const isFollowing = ref(props.initialFollowing);
const isSpecial = ref(props.initialSpecial);
const fansCount = ref(props.initialCount);
  const showDropdown = ref(false)
  
  const handleMainClick = () => {
    if (!isFollowing.value) {
      isFollowing.value = true
      fansCount.value += 1
    } else {
      showDropdown.value = !showDropdown.value
    }
  }
  
  const setSpecial = () => {
    isSpecial.value = true
    showDropdown.value = false
  }
  
  const unfollow = () => {
    isFollowing.value = false
    isSpecial.value = false
    fansCount.value -= 1
    showDropdown.value = false
  }
  </script>
  
  <style scoped>
  .follow-container {
    display: inline-block;
    position: relative;
  }
  
  .follow-wrapper {
    position: relative;
  }
  
  .follow-button {
    display: flex;
    align-items: center;
    padding: 8px 16px;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s ease;
    background: rgb(18, 150, 219);
    color: white;
    position: relative;
    overflow: hidden;
    width: 200px;
  
    .text {
      font-weight: 500;
      margin-right: 8px;
      margin-left: 30px;
    }
  
    .count {
    margin-top: 5px;
      font-size: 0.9em;
      opacity: 0.8;
    }
  
    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 3px 8px rgba(0, 132, 255, 0.3);
    }
  
    &.following {
      background: #666;
      &:hover {
        box-shadow: 0 3px 8px rgba(102, 102, 102, 0.3);
      }
    }
  
    &.special {
      background: linear-gradient(45deg, #ff9500, #ff6b6b);
    }
  }
  
  .dropdown-menu {
    position: absolute;
    top: 110%;
    left: 105px;
    width: 140px;
    background: white;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    overflow: hidden;
    z-index: 10;
  }
  
  .dropdown-item {
    padding: 10px 16px;
    cursor: pointer;
    transition: all 0.2s ease;
    display: flex;
    align-items: center;
  
    &:hover {
      background: #f5f5f5;
    }
  
    &.danger {
      color: #ff4444;
    }
  
    span {
      font-size: 0.95em;
    }
  }
  
  .dropdown-enter-active,
  .dropdown-leave-active {
    transition: all 0.25s ease;
    transform-origin: top;
  }
  
  .dropdown-enter-from,
  .dropdown-leave-to {
    opacity: 0;
    transform: translateY(-10px);
  }
  </style>