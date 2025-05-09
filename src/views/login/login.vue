<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '@/api/login';
import { ElMessage } from 'element-plus';

const router = useRouter();

const managerInfo = reactive({
    username: '',
    password: ''
});

const errors = reactive({
    username: '',
    password: ''
});

// 校验规则
const validateRules = {
    username: [
        { required: true, message: '请输入用户名' },
        { min: 4, max: 16, message: '用户名长度4-16个字符' },
        { pattern: /^[a-zA-Z0-9_]+$/, message: '只能包含字母数字下划线' }
    ],
    password: [
        { required: true, message: '请输入密码' },
        { min: 6, max: 20, message: '密码长度6-20个字符' },
        { 
            validator: (value) => {
                const hasLower = /[a-z]/.test(value);
                const hasUpper = /[A-Z]/.test(value);
                const hasNumber = /\d/.test(value);
                return hasLower && hasUpper && hasNumber;
            },
            message: '需包含大小写字母和数字'
        }
    ]
};

// 校验单个字段
const validateField = (fieldName) => {
    const value = managerInfo[fieldName];
    errors[fieldName] = '';
    
    for (const rule of validateRules[fieldName]) {
        if (rule.required && !value) {
            errors[fieldName] = rule.message;
            return false;
        }
        
        if (rule.min && value.length < rule.min) {
            errors[fieldName] = rule.message;
            return false;
        }
        
        if (rule.max && value.length > rule.max) {
            errors[fieldName] = rule.message;
            return false;
        }
        
        if (rule.pattern && !rule.pattern.test(value)) {
            errors[fieldName] = rule.message;
            return false;
        }
        
        if (rule.validator && !rule.validator(value)) {
            errors[fieldName] = rule.message;
            return false;
        }
    }
    
    return true;
};

// 表单提交
const managerLogin = async(e) => {
    e.preventDefault();
    
    // 验证所有字段
    const isUsernameValid = validateField('username');
    const isPasswordValid = validateField('password');
    
    if (!isUsernameValid || !isPasswordValid) {
        return;
    }
    
    try {
        const result = await login(managerInfo);
        if(result.code == 1){
            ElMessage.success('登录成功！');
            localStorage.setItem('loginManager', JSON.stringify(result.data));
            router.push('/index').then(() => {
                window.location.reload();
            });
        } else {
            ElMessage.error(result.msg);
        }
    } catch (error) {
        ElMessage.error('登录失败，请稍后重试');
    }
};

// 实时校验
const handleInput = (fieldName) => {
    validateField(fieldName);
};

onMounted(() => {
    document.body.classList.add('no-scroll');
});
</script>

<template>
 <div class="index-logo">
    <svg t="1746546358687" class="logo" viewBox="0 0 2304 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="3479" width="200" height="200"><path d="M1779.70087 322.593391c6.032696 1.001739 53.270261-9.527652 55.273739-6.021565 3.016348 4.507826 24.119652 155.926261 18.587826 156.928-5.520696 1.001739-44.722087 10.017391-44.722087 10.017391-3.016348-20.546783-28.638609-147.890087-29.139478-160.934956z m75.375304-14.547478l20.591304 162.949565c10.551652-0.50087 53.270261-3.517217 57.288348-4.006956-6.032696-61.673739-16.584348-158.942609-16.584348-158.942609a157.606957 157.606957 0 0 0-61.295304 0z m-40.704 226.114783s92.961391-23.56313 126.619826-12.02087c17.085217 61.662609 48.239304 407.596522 51.255652 421.643131-21.114435 2.504348-90.445913 8.525913-95.977739 10.017391-4.518957-26.067478-81.897739-403.088696-81.897739-419.639652z m344.186435-207.560348c5.531826 1.502609 54.761739-5.008696 55.262608-1.502609 1.001739 12.02087 6.032696 157.42887 0.50087 157.929739l-44.710957 4.006957c-1.001739-20.057043-12.054261-146.899478-11.052521-160.434087z m75.865043-4.006957l4.018087 160.934957c10.562783 0 52.758261 2.504348 57.277218 2.003478-1.001739-61.662609 0-158.942609 0-158.942609a278.049391 278.049391 0 0 0-61.295305-4.006956z m-64.812522 216.086261s94.96487-12.53287 127.120696 2.504348c7.034435 72.192 6.021565 408.609391 7.034435 422.656-21.604174 0-90.445913 1.001739-95.966609 2.003478-1.01287-26.078609-39.69113-410.624-38.188522-427.163826z m-220.582956-413.629217c54.77287 278.761739 96.467478 755.055304 97.981217 785.641739 0 0 43.219478 1.001739 91.948522 4.006956-28.638609-300.321391-63.810783-780.132174-63.298783-795.67026-12.076522-13.534609-126.630957 6.021565-126.630956 6.021565z m-120.587131 659.300174c-10.551652-78.213565-290.927304-166.956522-448.701217-138.384696 0 0-19.589565-172.466087-27.124869-339.422609-6.533565-143.393391-1.001739-282.768696 0.500869-305.830956-10.551652-7.524174-124.104348 46.625391-185.399652 69.186782 0 0 73.360696 309.849043 126.608696 952.598261 0 0 84.914087 9.015652 230.633739-19.055304 145.708522-28.070957 318.052174-115.812174 303.482434-219.091478z m-342.672695 140.877913l-24.631652-176.973913c6.032696-3.005217 157.273043 53.136696 173.356521 63.165217-2.515478 11.030261-148.72487 113.808696-148.724869 113.808696zM611.995826 322.582261c6.021565 1.001739 53.25913-9.516522 55.262609-6.010435 3.016348 4.507826 24.119652 155.926261 18.587826 156.928-5.520696 1.001739-44.710957 10.017391-44.710957 10.017391-3.005217-20.546783-28.638609-147.890087-29.139478-160.934956z m75.364174-14.536348l20.602435 162.949565c10.551652-0.50087 53.25913-3.517217 57.277217-4.006956-6.533565-61.673739-16.584348-158.942609-16.584348-158.942609a157.629217 157.629217 0 0 0-61.295304 0zM646.678261 534.160696s92.950261-23.56313 126.619826-12.02087c17.074087 61.662609 48.228174 407.596522 51.244522 421.643131-21.103304 2.504348-90.445913 8.525913-95.966609 10.017391-4.518957-26.067478-81.897739-403.088696-81.897739-419.639652zM990.831304 326.600348c5.531826 1.502609 54.761739-5.008696 55.273739-1.502609 1.001739 12.02087 6.021565 157.42887 0.50087 157.929739l-44.722087 4.006957c-1.001739-20.057043-11.553391-146.899478-11.052522-160.434087z m75.876174-4.006957l4.006957 160.934957c10.562783 0 52.769391 2.504348 57.288348 2.003478-1.001739-61.662609 0-158.942609 0-158.942609-20.101565-3.494957-40.69287-4.496696-61.295305-4.006956z m-64.823652 216.086261s94.96487-12.53287 127.120696 2.504348c7.034435 72.192 6.032696 408.609391 7.034435 422.656-21.604174 0-90.445913 1.001739-95.966609 2.003478-0.50087-26.078609-39.69113-410.624-38.177391-427.163826zM781.32313 125.050435C836.08487 403.812174 877.790609 880.105739 879.304348 910.692174c0 0 43.208348 1.001739 91.937391 4.006956-28.627478-300.321391-63.810783-780.132174-63.298782-796.16-12.065391-13.04487-126.619826 6.511304-126.619827 6.511305zM660.72487 784.350609c-10.540522-78.213565-290.916174-166.956522-448.690087-138.384696 0 0-19.589565-172.466087-27.136-339.422609-6.522435-143.393391-1.001739-282.768696 0.512-305.830956C174.85913-6.299826 61.306435 47.337739 0 69.89913c0 0 73.349565 309.849043 126.619826 952.598261 0 0 84.914087 9.015652 230.622609-19.055304 145.719652-28.070957 318.063304-115.812174 303.493565-219.091478zM318.052174 925.228522l-24.620522-176.973913c6.032696-3.005217 157.273043 53.136696 173.356522 63.165217-2.515478 11.030261-148.736 113.808696-148.736 113.808696z" p-id="3480" fill="#1296db"></path></svg>
 </div>
 <img class="login-bg-img" src="@\assets\pic\0307a34bcd194a8c243917a6a022a44a45dc6503.jpg">

 <!-- From Uiverse.io by Smit-Prajapati --> 
 <div class="container">
    <div class="heading">欢迎回来!</div>
    <form class="form" @submit="managerLogin">
      <div class="input-group">
        <input 
          class="input" 
          v-model="managerInfo.username" 
          @input="handleInput('username')"
          @blur="handleInput('username')"
          placeholder="用户名"
          :class="{ 'input-error': errors.username }"
        >
        <div class="error-message" v-if="errors.username">{{ errors.username }}</div>
      </div>
      
      <div class="input-group">
        <input 
          class="input" 
          v-model="managerInfo.password" 
          type="password" 
          @input="handleInput('password')"
          @blur="handleInput('password')"
          placeholder="密码"
          :class="{ 'input-error': errors.password }"
        >
        <div class="error-message" v-if="errors.password">{{ errors.password }}</div>
      </div>
      
      <span class="forgot-password"><a href="#">忘记密码 ?</a></span>
      <input class="login-button" type="submit" value="登录">
    </form>
    <div class="social-account-container">
        <span class="title">第三方登录</span>
        <div class="social-accounts">
          <button class="social-button google">
            <svg class="svg" xmlns="http://www.w3.org/2000/svg" height="1em" viewBox="0 0 488 512">
              <path d="M488 261.8C488 403.3 391.1 504 248 504 110.8 504 0 393.2 0 256S110.8 8 248 8c66.8 0 123 24.5 166.3 64.9l-67.5 64.9C258.5 52.6 94.3 116.6 94.3 256c0 86.5 69.1 156.6 153.7 156.6 98.2 0 135-70.4 140.8-106.9H248v-85.3h236.1c2.3 12.7 3.9 24.9 3.9 41.4z"></path>
            </svg></button>
          <button class="social-button apple">
            <svg class="svg" xmlns="http://www.w3.org/2000/svg" height="1em" viewBox="0 0 384 512">
              <path d="M318.7 268.7c-.2-36.7 16.4-64.4 50-84.8-18.8-26.9-47.2-41.7-84.7-44.6-35.5-2.8-74.3 20.7-88.5 20.7-15 0-49.4-19.7-76.4-19.7C63.3 141.2 4 184.8 4 273.5q0 39.3 14.4 81.2c12.8 36.7 59 126.7 107.2 125.2 25.2-.6 43-17.9 75.8-17.9 31.8 0 48.3 17.9 76.4 17.9 48.6-.7 90.4-82.5 102.6-119.3-65.2-30.7-61.7-90-61.7-91.9zm-56.6-164.2c27.3-32.4 24.8-61.9 24-72.5-24.1 1.4-52 16.4-67.9 34.9-17.5 19.8-27.8 44.3-25.6 71.9 26.1 2 49.9-11.4 69.5-34.3z"></path>
            </svg>
          </button>
          <button class="social-button twitter">
            <svg class="svg" xmlns="http://www.w3.org/2000/svg" height="1em" viewBox="0 0 512 512">
              <path d="M389.2 48h70.6L305.6 224.2 487 464H345L233.7 318.6 106.5 464H35.8L200.7 275.5 26.8 48H172.4L272.9 180.9 389.2 48zM364.4 421.8h39.1L151.1 88h-42L364.4 421.8z"></path>
            </svg>
          </button>
        </div>
      </div>
      <span class="agreement"><a href="#">(゜-゜)つロ 干杯~</a></span>
  </div>
</template>

<style scoped>
.input-group {
  position: relative;
  margin-bottom: 20px;
}

.error-message {
  color: #ff4d4f;
  font-size: 12px;
  margin-top: 5px;
  margin-left: 15px;
}

.input-error {
  border-color: #ff4d4f !important;
}

.input-error:focus {
  border-color: #ff4d4f !important;
  box-shadow: 0 0 0 2px rgba(255, 77, 79, 0.2) !important;
}

/* 禁止滚动并固定滚动条宽度 */
.no-scroll {
  overflow: hidden;
  /* 滚动条宽度需与全局样式一致，避免布局抖动 */
  width: calc(100% - 7px); /* 假设滚动条宽度为7px  */
}

.index-logo{
    position: absolute;
    top: -60px;
    left: 20px;
}

.logo{
    width: 120px;
}

/* From Uiverse.io by Smit-Prajapati */ 
.container {
    position: absolute;
    top: 150px;
    left: 800px;
  max-width: 350px;
  background: #F8F9FD;
  background: linear-gradient(0deg, rgb(255, 255, 255) 0%, rgb(244, 247, 251) 100%);
  border-radius: 40px;
  padding: 25px 35px;
  border: 5px solid rgb(255, 255, 255);
  box-shadow: rgba(133, 189, 215, 0.8784313725) 0px 30px 30px -20px;
  margin: 20px;
}

.heading {
  text-align: center;
  font-weight: 900;
  font-size: 30px;
  color: rgb(16, 137, 211);
}

.form {
  margin-top: 20px;
}

.form .input {
  width: 305px;
  background: white;
  border: none;
  padding: 15px 20px;
  border-radius: 20px;
  margin-top: 15px;
  box-shadow: #cff0ff 0px 10px 10px -5px;
  border-inline: 2px solid transparent;
}

.form .input::-moz-placeholder {
  color: rgb(170, 170, 170);
}

.form .input::placeholder {
  color: rgb(170, 170, 170);
}

.form .input:focus {
  outline: none;
  border-inline: 2px solid #12B1D1;
}

.form .forgot-password {
  display: block;
  margin-top: 10px;
  margin-left: 10px;
}

.form .forgot-password a {
  font-size: 11px;
  color: #0099ff;
  text-decoration: none;
}

.form .login-button {
  display: block;
  width: 100%;
  font-weight: bold;
  background: linear-gradient(45deg, rgb(16, 137, 211) 0%, rgb(18, 177, 209) 100%);
  color: white;
  padding-block: 15px;
  margin: 20px auto;
  border-radius: 20px;
  box-shadow: rgba(133, 189, 215, 0.8784313725) 0px 20px 10px -15px;
  border: none;
  transition: all 0.2s ease-in-out;
}

.form .login-button:hover {
  transform: scale(1.03);
  box-shadow: rgba(133, 189, 215, 0.8784313725) 0px 23px 10px -20px;
}

.form .login-button:active {
  transform: scale(0.95);
  box-shadow: rgba(133, 189, 215, 0.8784313725) 0px 15px 10px -10px;
}

.social-account-container {
  margin-top: 25px;
}

.social-account-container .title {
  display: block;
  text-align: center;
  font-size: 10px;
  color: rgb(170, 170, 170);
}

.social-account-container .social-accounts {
  width: 100%;
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 5px;
}

.social-account-container .social-accounts .social-button {
  background: linear-gradient(45deg, rgb(0, 0, 0) 0%, rgb(112, 112, 112) 100%);
  border: 5px solid white;
  padding: 5px;
  border-radius: 50%;
  width: 40px;
  aspect-ratio: 1;
  display: grid;
  place-content: center;
  box-shadow: rgba(133, 189, 215, 0.8784313725) 0px 12px 10px -8px;
  transition: all 0.2s ease-in-out;
}

.social-account-container .social-accounts .social-button .svg {
  fill: white;
  margin: auto;
}

.social-account-container .social-accounts .social-button:hover {
  transform: scale(1.2);
}

.social-account-container .social-accounts .social-button:active {
  transform: scale(0.9);
}

.agreement {
  display: block;
  text-align: center;
  margin-top: 15px;
}

.agreement a {
  text-decoration: none;
  color: #0099ff;
  font-size: 9px;
}

.login-bg-img{
    top: 0px;
    height: 900px;
    position: absolute;
    left: 200px;
    z-index: -1;
}
</style>
