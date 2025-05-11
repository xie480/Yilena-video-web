d<script setup>
import { ref,watch } from 'vue';
import { Form, Field, ErrorMessage, useForm } from 'vee-validate';
import * as yup from 'yup';
import { useField } from 'vee-validate';
import { ElMessage } from 'element-plus';
import { register,login } from '../../api/login';
import { useRouter } from "vue-router";

const container = ref(null);
const router = useRouter();

const { value, errorMessage, handleBlur } = useField('username', (value) => {
  if (!value) return '用户名必填';
  if (value.length < 3) return '至少3个字符';
  return true;
});

// 新增密码相关验证
const { 
  value: passwordValue,
  errorMessage: passwordError,
  handleBlur: passwordBlur 
} = useField(
  'password',
  yup.string()
    .required('密码必填')
    .min(8, '密码至少8位')
    .matches(
      /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[A-Za-z\d@$!%*?&]+$/,
      '必须包含大小写字母和数字'
    )
);

// 确认密码验证
const { 
  value: confirmPasswordValue,
  errorMessage: confirmPasswordError,
  handleBlur: confirmPasswordBlur 
} = useField(
  'confirmPassword',
  yup.string()
    .required('请确认密码')
);

let userInfo = ref({username:'', password:''});
let loginUserInfo = ref({username:'', password:''});

watch([passwordValue, confirmPasswordValue], () => {
  userInfo.value = {
    username: value.value,
    password: passwordValue.value,
  }
})

const registerUser = async() => {
    console.log(userInfo.value);
	userInfo.value = {
    username: value.value,
    password: passwordValue.value,
  }
    if(passwordValue.value !== confirmPasswordValue.value) {
        ElMessage.error('两次输入的密码不一致');	
    }else{
  if (errorMessage.value || passwordError.value || confirmPasswordError.value) {
    ElMessage.error('输入不合法，请检查输入！');
  }else{

  const result = await register(userInfo.value);
  if (result.code == 1) {
     ElMessage.success('注册成功!请返回登录！');
   } else {
     ElMessage.error(result.msg);
   }
}
}  
};

const loginUser = async() => {
    if(loginUserInfo.value.username == ''|| loginUserInfo.value.password == '') {
        ElMessage.error('用户名或密码不能为空！');	
    }else{
        const result = await login(loginUserInfo.value);
        if (result.code == 1) {
            ElMessage.success('登录成功');
            localStorage.setItem('loginUser', JSON.stringify(result.data));
            router.push('/index').then(() => {
                window.location.reload();
            })
        } else {
            ElMessage.error(result.msg);
        }
    }
};

const handleSignUp = () => {
  container.value.classList.add("right-panel-active");
  userInfo.value = {username:'', password:''};
};

const handleSignIn = () => {
  container.value.classList.remove("right-panel-active");
  userInfo.value = {username:'', password:''};
};
</script>

<template>
    <div class="bg-img"></div>
    <div class="download"></div>
    <div class="logo"></div>
<div class="container" ref="container" id="container">
	<div class="form-container sign-up-container">
		<form action="#">
			<h1 class="sp-h1">注册账号</h1>
			<div class="social-container">
				<svg t="1743821788898" class="icon" viewBox="0 0 1025 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="2547" width="200" height="200"><path d="M511.87892 0C298.937289 0 104.4233 137.183551 30.712735 335.792572-42.997829 538.496624 20.475157 769.865896 186.323927 907.049447c165.84877 137.183551 405.408105 155.611192 589.684516 45.045345 184.276411-110.565847 280.509648-329.650025 239.559335-540.54414C972.569948 202.704052 800.578631 38.902798 591.732032 6.142547 565.114328 2.047516 538.496624 0 511.87892 0zM870.194164 647.014955c0 20.475157-2.047516 49.140376-18.427641 63.472986-14.33261 14.33261-26.617704-2.047516-34.807767-14.33261-2.047516-4.095031-14.33261-28.66522-16.380125-28.66522-6.142547 0-12.285094 24.570188-12.285094 28.66522-10.237578 24.570188-24.570188 45.045345-42.997829 63.472986 18.427641 18.427641 81.900627 30.712735 65.520502 69.615533-14.33261 34.807767-71.663049 40.950314-102.375784 42.997829-40.950314 2.047516-85.995659-2.047516-124.898457-14.33261-20.475157-6.142547-36.855282-12.285094-59.377955-12.285094-18.427641-2.047516-36.855282 16.380125-55.282923 22.522672-40.950314 14.33261-90.09069 12.285094-131.041004 8.190063-28.66522-4.095031-88.043174-10.237578-94.185721-47.092861-2.047516-12.285094 2.047516-30.712735 10.237578-40.950314 6.142547-6.142547 12.285094-10.237578 20.475157-12.285094 8.190063-2.047516 22.522672-2.047516 28.66522-6.142547 6.142547-4.095031-4.095031-8.190063-8.190063-12.285094-8.190063-6.142547-16.380125-14.33261-22.522672-20.475157-14.33261-16.380125-24.570188-32.760251-30.712735-51.187892-2.047516-4.095031-6.142547-24.570188-8.190063-24.570188-4.095031-2.047516-4.095031 6.142547-6.142547 8.190063-8.190063 16.380125-22.522672 34.807767-40.950314 40.950314-20.475157 8.190063-22.522672-12.285094-24.570188-28.66522-2.047516-20.475157-2.047516-42.997829 4.095031-63.472986 10.237578-40.950314 32.760251-77.805596 61.42547-108.518331 8.190063-8.190063 16.380125-16.380125 26.617704-24.570188 10.237578-8.190063 8.190063-6.142547 8.190063-18.427641-2.047516-22.522672-6.142547-30.712735 4.095031-53.235408 4.095031-8.190063 16.380125-20.475157 18.427641-28.66522 4.095031-10.237578 4.095031-18.427641 6.142547-28.66522 2.047516-20.475157 10.237578-40.950314 18.427641-59.377955 18.427641-38.902798 47.092861-75.75808 79.853112-102.375784 67.568017-53.235408 171.991317-55.282923 249.796913-28.66522 88.043174 28.66522 139.231066 104.4233 151.51616 194.51399 2.047516 8.190063 2.047516 18.427641 4.095031 26.617704 4.095031 10.237578 12.285094 18.427641 14.33261 26.617704 8.190063 20.475157 6.142547 36.855282 2.047516 57.330439-4.095031 16.380125 18.427641 38.902798 26.617704 53.235408 10.237578 20.475157 20.475157 40.950314 28.66522 61.42547C864.051617 604.017126 870.194164 626.539798 870.194164 647.014955z" p-id="2548"></path></svg>
				<svg t="1743821982657" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="3593" width="200" height="200"><path d="M579.867 511.932c-11.67 0-23.344 10.837-23.344 24.178 0 10.839 11.668 21.678 23.344 21.678 17.514 0 30.020-10.839 30.020-21.678 0-13.34-12.505-24.178-30.020-24.178v0zM503.154 399.36c18.345 0 30.020-11.67 30.020-29.185 0-18.345-11.675-29.183-30.020-29.183-17.51 0-34.188 10.839-34.188 29.183-0.001 17.517 16.678 29.185 34.188 29.185v0zM512.326 0.56c-282.655 0-511.788 229.133-511.788 511.788 0 282.653 229.133 511.786 511.788 511.786 282.653 0 511.786-229.133 511.786-511.786 0.001-282.655-229.132-511.788-511.786-511.788v0zM415.602 642.009c-30.855 0-53.369-4.999-82.553-13.341l-84.218 42.53 24.186-71.715c-59.204-41.689-94.228-94.221-94.228-158.428 0-113.403 106.733-200.123 236.814-200.123 115.069 0 217.633 68.377 237.644 165.106-8.338-1.671-15.844-2.502-22.514-2.502-113.403 0-200.954 85.049-200.954 187.613 0 17.508 2.502 33.351 6.671 50.030-6.671 0.829-14.178 0.829-20.849 0.829v0zM763.314 723.724l16.674 60.041-63.371-35.854c-24.18 4.999-47.529 12.505-71.709 12.505-111.737 0-200.123-76.711-200.123-171.769 0-95.058 88.388-171.771 200.123-171.771 105.895 0 200.954 76.713 200.954 171.771 0 53.362-35.854 100.891-82.548 135.078v0zM338.053 340.992c-17.51 0-35.854 10.839-35.854 29.183 0 17.516 18.343 29.19 35.854 29.19 16.679 0 30.020-11.675 30.020-29.19 0-18.343-13.341-29.183-30.020-29.183v0zM710.783 511.932c-12.51 0-23.349 10.837-23.349 24.178 0 10.839 10.839 21.678 23.349 21.678 16.674 0 29.183-10.839 29.183-21.678 0-13.34-12.509-24.178-29.183-24.178v0zM710.783 511.932z" p-id="3594"></path></svg>
                <svg t="1743822075012" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="5453" width="200" height="200"><path d="M588.288 998.912V651.776h114.176l17.408-137.728H588.288v-88.064c0-22.528 4.608-38.912 13.312-50.176 9.216-11.264 26.112-16.896 52.224-16.896h70.656V235.52c-24.064-3.584-58.368-5.632-102.4-5.632-51.712 0-93.184 15.872-124.416 47.616s-46.592 76.8-46.592 134.656v101.376H336.384v137.728H450.56v349.696C207.36 970.752 18.944 763.392 18.944 512c0-272.384 220.672-493.568 493.568-493.568S1006.08 239.104 1006.08 512c0 246.784-181.248 450.56-417.792 486.912z m-75.264 6.656H512h1.024z"  p-id="5454"></path></svg>
            </div>
			<input type="text" placeholder="请输入用户名" v-model="value" @blur="handleBlur" name="username" :class="{ 'error-border': usernameError }"/>
            <span class="error-text">{{ errorMessage }}</span>
			 <!-- 密码输入 -->
             <input 
      type="password" 
      placeholder="请输入密码"
      v-model="passwordValue"
      @blur="passwordBlur"
      :class="{ 'error-border': passwordError }"
      name = "password"
    />
    <span class="error-text">{{ passwordError }}</span>

    <!-- 确认密码输入 -->
    <input 
      type="password" 
      placeholder="请再次输入密码"
      v-model="confirmPasswordValue"
      @blur="confirmPasswordBlur"
      :class="{ 'error-border': confirmPasswordError }"
    />
    <span class="error-text">{{ confirmPasswordError }}</span>
			<button type="button" class="ghost-sp" @click="registerUser">注册</button>
		</form>
	</div>
	<div class="form-container sign-in-container">
		<form action="#">
			<h1 class="si-h1">登录账号</h1>
			<div class="social-container">
				<svg t="1743821788898" class="icon" viewBox="0 0 1025 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="2547" width="200" height="200"><path d="M511.87892 0C298.937289 0 104.4233 137.183551 30.712735 335.792572-42.997829 538.496624 20.475157 769.865896 186.323927 907.049447c165.84877 137.183551 405.408105 155.611192 589.684516 45.045345 184.276411-110.565847 280.509648-329.650025 239.559335-540.54414C972.569948 202.704052 800.578631 38.902798 591.732032 6.142547 565.114328 2.047516 538.496624 0 511.87892 0zM870.194164 647.014955c0 20.475157-2.047516 49.140376-18.427641 63.472986-14.33261 14.33261-26.617704-2.047516-34.807767-14.33261-2.047516-4.095031-14.33261-28.66522-16.380125-28.66522-6.142547 0-12.285094 24.570188-12.285094 28.66522-10.237578 24.570188-24.570188 45.045345-42.997829 63.472986 18.427641 18.427641 81.900627 30.712735 65.520502 69.615533-14.33261 34.807767-71.663049 40.950314-102.375784 42.997829-40.950314 2.047516-85.995659-2.047516-124.898457-14.33261-20.475157-6.142547-36.855282-12.285094-59.377955-12.285094-18.427641-2.047516-36.855282 16.380125-55.282923 22.522672-40.950314 14.33261-90.09069 12.285094-131.041004 8.190063-28.66522-4.095031-88.043174-10.237578-94.185721-47.092861-2.047516-12.285094 2.047516-30.712735 10.237578-40.950314 6.142547-6.142547 12.285094-10.237578 20.475157-12.285094 8.190063-2.047516 22.522672-2.047516 28.66522-6.142547 6.142547-4.095031-4.095031-8.190063-8.190063-12.285094-8.190063-6.142547-16.380125-14.33261-22.522672-20.475157-14.33261-16.380125-24.570188-32.760251-30.712735-51.187892-2.047516-4.095031-6.142547-24.570188-8.190063-24.570188-4.095031-2.047516-4.095031 6.142547-6.142547 8.190063-8.190063 16.380125-22.522672 34.807767-40.950314 40.950314-20.475157 8.190063-22.522672-12.285094-24.570188-28.66522-2.047516-20.475157-2.047516-42.997829 4.095031-63.472986 10.237578-40.950314 32.760251-77.805596 61.42547-108.518331 8.190063-8.190063 16.380125-16.380125 26.617704-24.570188 10.237578-8.190063 8.190063-6.142547 8.190063-18.427641-2.047516-22.522672-6.142547-30.712735 4.095031-53.235408 4.095031-8.190063 16.380125-20.475157 18.427641-28.66522 4.095031-10.237578 4.095031-18.427641 6.142547-28.66522 2.047516-20.475157 10.237578-40.950314 18.427641-59.377955 18.427641-38.902798 47.092861-75.75808 79.853112-102.375784 67.568017-53.235408 171.991317-55.282923 249.796913-28.66522 88.043174 28.66522 139.231066 104.4233 151.51616 194.51399 2.047516 8.190063 2.047516 18.427641 4.095031 26.617704 4.095031 10.237578 12.285094 18.427641 14.33261 26.617704 8.190063 20.475157 6.142547 36.855282 2.047516 57.330439-4.095031 16.380125 18.427641 38.902798 26.617704 53.235408 10.237578 20.475157 20.475157 40.950314 28.66522 61.42547C864.051617 604.017126 870.194164 626.539798 870.194164 647.014955z" p-id="2548"></path></svg>
				<svg t="1743821982657" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="3593" width="200" height="200"><path d="M579.867 511.932c-11.67 0-23.344 10.837-23.344 24.178 0 10.839 11.668 21.678 23.344 21.678 17.514 0 30.020-10.839 30.020-21.678 0-13.34-12.505-24.178-30.020-24.178v0zM503.154 399.36c18.345 0 30.020-11.67 30.020-29.185 0-18.345-11.675-29.183-30.020-29.183-17.51 0-34.188 10.839-34.188 29.183-0.001 17.517 16.678 29.185 34.188 29.185v0zM512.326 0.56c-282.655 0-511.788 229.133-511.788 511.788 0 282.653 229.133 511.786 511.788 511.786 282.653 0 511.786-229.133 511.786-511.786 0.001-282.655-229.132-511.788-511.786-511.788v0zM415.602 642.009c-30.855 0-53.369-4.999-82.553-13.341l-84.218 42.53 24.186-71.715c-59.204-41.689-94.228-94.221-94.228-158.428 0-113.403 106.733-200.123 236.814-200.123 115.069 0 217.633 68.377 237.644 165.106-8.338-1.671-15.844-2.502-22.514-2.502-113.403 0-200.954 85.049-200.954 187.613 0 17.508 2.502 33.351 6.671 50.030-6.671 0.829-14.178 0.829-20.849 0.829v0zM763.314 723.724l16.674 60.041-63.371-35.854c-24.18 4.999-47.529 12.505-71.709 12.505-111.737 0-200.123-76.711-200.123-171.769 0-95.058 88.388-171.771 200.123-171.771 105.895 0 200.954 76.713 200.954 171.771 0 53.362-35.854 100.891-82.548 135.078v0zM338.053 340.992c-17.51 0-35.854 10.839-35.854 29.183 0 17.516 18.343 29.19 35.854 29.19 16.679 0 30.020-11.675 30.020-29.19 0-18.343-13.341-29.183-30.020-29.183v0zM710.783 511.932c-12.51 0-23.349 10.837-23.349 24.178 0 10.839 10.839 21.678 23.349 21.678 16.674 0 29.183-10.839 29.183-21.678 0-13.34-12.509-24.178-29.183-24.178v0zM710.783 511.932z" p-id="3594"></path></svg>
                <svg t="1743822075012" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="5453" width="200" height="200"><path d="M588.288 998.912V651.776h114.176l17.408-137.728H588.288v-88.064c0-22.528 4.608-38.912 13.312-50.176 9.216-11.264 26.112-16.896 52.224-16.896h70.656V235.52c-24.064-3.584-58.368-5.632-102.4-5.632-51.712 0-93.184 15.872-124.416 47.616s-46.592 76.8-46.592 134.656v101.376H336.384v137.728H450.56v349.696C207.36 970.752 18.944 763.392 18.944 512c0-272.384 220.672-493.568 493.568-493.568S1006.08 239.104 1006.08 512c0 246.784-181.248 450.56-417.792 486.912z m-75.264 6.656H512h1.024z"  p-id="5454"></path></svg>
			</div>
			<input type="text" placeholder="请输入用户名" v-model="loginUserInfo.username"/>
			<input type="password" placeholder="请输入密码" v-model="loginUserInfo.password"/>
			<a href="/404">忘记密码</a>
			<button type="button" class="si-btn" @click="loginUser">登录</button>
		</form>
	</div>
	<div class="overlay-container">
		<div class="overlay">
			<div class="overlay-panel overlay-left">
				<h1>&nbsp;&nbsp;很高兴遇见你！</h1>
				<p>Nice to meet you!</p>
				<button class="ghost" id="signIn" @click="handleSignIn">返回登录</button>
			</div>
			<div class="overlay-panel overlay-right">
				<h1>&nbsp;&nbsp;欢迎回来！</h1>
				<p>Welcome back!</p>
				<button class="ghost" id="signUp" @click="handleSignUp">前往注册</button>
			</div>
		</div>
	</div>
</div>
</template>

<style scoped>

@import url('https://fonts.googleapis.com/css?family=Montserrat:400,800');

* {
	box-sizing: border-box;
}

body {
	background: #f6f5f7;
	display: flex;
	justify-content: center;
	align-items: center;
	flex-direction: column;
	font-family: 'Montserrat', sans-serif;
	height: 100vh;
	margin: -20px 0 50px;
}

h1 {
	font-weight: bold;
	margin: 0;
}

h2 {
	text-align: center;
}

p {
	font-size: 14px;
	font-weight: 100;
	line-height: 20px;
	letter-spacing: 0.5px;
	margin: 20px 0 30px;
}

span {
	font-size: 12px;
}

a {
	color: #333;
	font-size: 14px;
	text-decoration: none;
	margin: 15px 0;
}

button {
	border-radius: 20px;
	border: 1px solid #FF4B2B;
	background-color: #FF4B2B;
	color: #FFFFFF;
	font-size: 12px;
	font-weight: bold;
	padding: 12px 45px;
	letter-spacing: 1px;
	text-transform: uppercase;
	transition: transform 80ms ease-in;
}

button:active {
	transform: scale(0.95);
}

button:focus {
	outline: none;
}

button.ghost {
	background-color: transparent;
	border-color: #FFFFFF;
}

form {
	background-color: #FFFFFF;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-direction: column;
	padding: 0 50px;
	height: 100%;
	text-align: center;
}

input {
	background-color: #eee;
	border: none;
	padding: 12px 15px;
	margin: 8px 0;
	width: 100%;
}

.container {
	background-color: #fff;
	border-radius: 10px;
  	box-shadow: 0 14px 28px rgba(0,0,0,0.25), 
			0 10px 10px rgba(0,0,0,0.22);
	position: relative;
	overflow: hidden;
	width: 768px;
	max-width: 100%;
	min-height: 480px;
    position: absolute;
    top: 50%;
    left: 470px;
    transform: translate(-50%, -50%);
}

.form-container {
	position: absolute;
	top: 0;
	height: 100%;
	transition: all 0.6s ease-in-out;
}

.sign-in-container {
	left: 0;
	width: 50%;
	z-index: 2;
}

.container.right-panel-active .sign-in-container {
	transform: translateX(100%);
}

.sign-up-container {
	left: 0;
	width: 50%;
	opacity: 0;
	z-index: 1;
}

.container.right-panel-active .sign-up-container {
	transform: translateX(100%);
	opacity: 1;
	z-index: 5;
	animation: show 0.6s;
}

@keyframes show {
	0%, 49.99% {
		opacity: 0;
		z-index: 1;
	}
	
	50%, 100% {
		opacity: 1;
		z-index: 5;
	}
}

.overlay-container {
	position: absolute;
	top: 0;
	left: 50%;
	width: 50%;
	height: 100%;
	overflow: hidden;
	transition: transform 0.6s ease-in-out;
	z-index: 100;
}

.container.right-panel-active .overlay-container{
	transform: translateX(-100%);
}

.overlay {
	background: #FF416C;
	background: -webkit-linear-gradient(to right, #FF4B2B, #FF416C);
	background: linear-gradient(to right, #FF4B2B, #FF416C);
	background-repeat: no-repeat;
	background-size: cover;
	background-position: 0 0;
	color: #FFFFFF;
	position: relative;
	left: -100%;
	height: 100%;
	width: 200%;
  	transform: translateX(0);
	transition: transform 0.6s ease-in-out;
}

.container.right-panel-active .overlay {
  	transform: translateX(50%);
}

.overlay-panel {
	position: absolute;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-direction: column;
	padding: 0 40px;
	text-align: center;
	top: 0;
	height: 100%;
	width: 50%;
	transform: translateX(0);
	transition: transform 0.6s ease-in-out;
}

.overlay-left {
	transform: translateX(-20%);
    background-color: rgba(251, 114, 153);
}

.container.right-panel-active .overlay-left {
	transform: translateX(0);
}

.overlay-right {
	right: 0;
	transform: translateX(0);
    background-color: rgba(0, 158, 210);
}

.container.right-panel-active .overlay-right {
	transform: translateX(20%);
}

.social-container {
	margin: 20px 0;
}

.social-container a {
	border: 1px solid #DDDDDD;
	border-radius: 50%;
	display: inline-flex;
	justify-content: center;
	align-items: center;
	margin: 0 5px;
	height: 40px;
	width: 40px;
}

footer {
    background-color: #222;
    color: #fff;
    font-size: 14px;
    bottom: 0;
    position: fixed;
    left: 0;
    right: 0;
    text-align: center;
    z-index: 999;
}

footer p {
    margin: 10px 0;
}

footer i {
    color: red;
}

footer a {
    color: #3c97bf;
    text-decoration: none;
}

.ghost-sp{
    margin-top: 20px;
    background-color: rgba(251, 114, 153);
}

.icon{
    width: 35px;
    height: 35px;
    margin: 0 10px;
    fill:#33333381 !important;
}

.sp-h1{
    color: rgba(251, 114, 153);
}

.si-btn{
    background-color: rgba(0, 158, 210);
    border: none;
}

.si-h1{
    color: rgba(0, 158, 210);
}

.bg-img{
    position: absolute;
  z-index: -1; /* 确保在内容层下方 */
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url('@/assets/pic/77490840dbd32f36f1cfd614a94f1c40dd226cac.png');
  background-size: cover;
  background-position: center;
}

.download{
    position: absolute;
  z-index: 0; 
  top: 600px;
  left: 1425px;
  width: 250px;
  height: 200px;
  background-image: url('@/assets/pic/app-download.png');
  background-size: cover;
  background-position: center;
}

.logo{
    position: absolute;
  z-index: 0; 
  top: 0px;
  left: -125px;
  width: 250px;
  height: 100px;
  background-image: url('@/assets/pic/图标.png');
  background-size: cover;
  background-position: center;
}
.error-border { border: 1px solid #ff4d4f; }
.error-text { color: #ff4d4f; font-size: 12px; }
</style>
