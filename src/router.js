import Vue from "vue";
import Router from "vue-router";

import Search from "@/views/Search.vue";
import Musics from "@/views/Musics.vue";
import Playlists from "@/views/Playlists.vue";
import Settings from "@/views/Settings.vue";

import { routerMode } from "platform";

Vue.use(Router);

export const routes = [
  { path: "/", redirect: "/musics" },
  {
    path: "/search",
    component: Search,
    name: "search",
    props: route => ({ query: route.query.q }),
    meta: {
      name_fr: "Rechercher",
      icon: "search",
      link: true
    }
  },
  {
    path: "/musics",
    component: Musics,
    name: "musics",
    meta: {
      name_fr: "Musiques",
      icon: "library_music",
      link: true
    }
  },
  {
    path: "/playlists",
    component: Playlists,
    name: "playlists",
    meta: {
      name_fr: "Playlists",
      icon: "queue_music",
      mobileOnly: true,
      link: true
    }
  },
  {
    path: "/playlists/:playlist",
    name: "playlist",
    component: Musics,
    props: true
  },
  {
    path: "/settings",
    component: Settings,
    name: "settings",
    meta: {
      name_fr: "Paramètres",
      icon: "settings",
      link: true
    }
  }
];

export default new Router({
  mode: routerMode,
  routes
});
