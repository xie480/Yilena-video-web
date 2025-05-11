import { createRouter, createWebHistory } from 'vue-router'
import IndexView from '@/views/index/index.vue'
import ChatView from '@/views/chat/chat.vue'
import HistoryView from '@/views/history/history.vue'
import PostView from '@/views/post/post.vue'
import SearchView from '@/views/search/search.vue'
import UploadView from '@/views/upload/upload.vue'
import VideoView from '@/views/video/video.vue'
import LoginView from '@/views/login/login.vue'
import ErrorView from '@/views/404/404.vue'
import DefaultSearch from '@/views/search/defaultSearch.vue'
import UserSearch from '@/views/search/userSearch.vue'
import VideoSearch from '@/views/search/videoSearch.vue'
import commentManagement from '../views/upload/commentManagement.vue'
import dataCenter from '../views/upload/dataCenter.vue'
import fileManagement from '../views/upload/fileManagement.vue'
import uploadIndex from '../views/upload/index.vue'
import uploadVideo from '../views/upload/uploadVideo.vue'
import UserInfoView from '@/views/userInfo/mine/userInfo.vue'
import userInfoIndex from '../views/userInfo/mine/userInfoIndex.vue'
import userInfoPost from '../views/userInfo/mine/post.vue'
import userInfoUpload from '../views/userInfo/mine/upload.vue'
import userInfoCollection from '../views/userInfo/mine/collection.vue'
import userInfoFavorites from '../views/userInfo/mine/favorites.vue'
import UserInfoViewOthers from '@/views/userInfo/others/userInfo.vue'
import userInfoIndexOthers from '../views/userInfo/others/userInfoIndex.vue'
import userInfoPostOthers from '../views/userInfo/others/post.vue'
import userInfoUploadOthers from '../views/userInfo/others/upload.vue'
import userInfoCollectionOthers from '../views/userInfo/others/collection.vue'
import userInfoFavoritesOthers from '../views/userInfo/others/favorites.vue'
import MyMessage from '../views/chat/MyMessage.vue'
import AnswerMe from '../views/chat/AnswerMe.vue'
import AtMe from '../views/chat/AtMe.vue'
import LikeMe from '../views/chat/LikeMe.vue'
import SystemMessage from '../views/chat/SystemMessage.vue'
import OtherMessage from '../views/chat/OtherMessage.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/index'}, 
    {path: '/index', name: 'index', component: IndexView}
    ,{path: '/chat', 
      name: 'chat', 
      component: ChatView,
      redirect:'/chat/MyMessage',
      children:[
       {
        path: 'MyMessage',
        name: 'MyMessage',
        component: MyMessage,
        props: true
       } ,
       {
        path: 'AnswerMe',
        name: 'AnswerMe',
        component: AnswerMe,
        props: true
       },
       {
        path: 'AtMe',
        name: 'AtMe',
        component: AtMe,
        props: true
       },
       {
        path: 'LikeMe',
        name: 'LikeMe',
        component: LikeMe,
        props: true
       },
       {
        path: 'SystemMessage',
        name: 'SystemMessage',
        component: SystemMessage,
        props: true
       },
       {
        path: 'OtherMessage',
        name: 'OtherMessage',
        component: OtherMessage,
        props: true
       }
      ]
    }
    ,{path: '/history', name: 'history', component: HistoryView}
    ,{path: '/post', name: 'post', component: PostView}
   ,{
    path: '/search/:query', // 主路径携带参数
    name: 'search',
    component: SearchView,
    // 动态重定向到默认子路由
    redirect: to => ({
      path: `/search/${to.params.query}/default`
    }),
    children: [
      {
        path: 'user', // 子路由继承参数
        name: 'user-search',
        component: UserSearch,
        props: true // 自动传递路由参数为 props
      },
      {
        path: 'video',
        name: 'video-search',
        component: VideoSearch,
        props: true
      },
      {
        path: 'default',
        name: 'default-search',
        component: DefaultSearch,
        props: true
      }
    ]
  }
   ,{path: '/userInfo/:userId', 
    name: 'userInfo', 
    component: UserInfoView, 
    props: true,
    redirect: to => ({
      path: `/userInfo/${to.params.userId}/index`
    }),
    children: [
      {
        path: 'index',
        name: 'userInfo-index',
        component: userInfoIndex,
        props: true
      },
      {
        path: 'post',
        name: 'userInfo-post',
        component: userInfoPost,
        props: true
      },
      {
        path: 'upload',
        name: 'userInfo-upload',
        component: userInfoUpload,
        props: true
      },
      {
        path: 'collection',
        name: 'userInfo-collection',
        component: userInfoCollection,
        props: true
      },
      {
        path: 'favorites',
        name: 'userInfo-favorites',
        component: userInfoFavorites,
        props: true
      }
    ]
   },
   ,{path: '/userInfo/others/:userId', 
    name: 'userInfo-ohters', 
    component: UserInfoViewOthers, 
    props: true,
    redirect: to => ({
      path: `/userInfo/others/${to.params.userId}/index`
    }),
    children: [
      {
        path: 'index',
        name: 'userInfo-index-others',
        component: userInfoIndexOthers,
        props: true
      },
      {
        path: 'post',
        name: 'userInfo-post-others',
        component: userInfoPostOthers,
        props: true
      },
      {
        path: 'upload',
        name: 'userInfo-upload-others',
        component: userInfoUploadOthers,
        props: true
      },
      {
        path: 'collection',
        name: 'userInfo-collection-others',
        component: userInfoCollectionOthers,
        props: true
      },
      {
        path: 'favorites',
        name: 'userInfo-favorites-others',
        component: userInfoFavoritesOthers,
        props: true
      }
    ]
   }
   ,{
    path: '/upload', 
    name: 'upload', 
    component: UploadView,
    redirect:'/upload/uploadIndex',
    children:[
      {
        path:'uploadVideo',
        name:'upload-video',
        component:uploadVideo,
        props:true
      },
      {
        path:'uploadIndex',
        name:'upload-index',
        component:uploadIndex,
        props:true
      },
      {
        path:'commentManagement',
        name:'commentManagement',
        component:commentManagement,
        props:true
      },
      {
        path:'dataCenter',
        name:'dataCenter',
        component:dataCenter,
        props:true
      },
      {
        path:'fileManagement',
        name:'fileManagement',
        component:fileManagement,
        props:true
      }
    ]
  }
   ,{path: '/video/:videoId', name: 'video', component: VideoView, props: true},
  {path: '/login', name: 'login', component: LoginView},
  {path: '/404', name: '404', component: ErrorView},
  ]
})

export default router