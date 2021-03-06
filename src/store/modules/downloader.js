import Vue from "vue";
import { providers } from "platform";

export const state = {
  progress: {}
};

const types = {
  SET_PROGRESS: "SET_PROGRESS",
  REMOVE_PROGRESS: "REMOVE_PROGRESS"
};

export const mutations = {
  [types.SET_PROGRESS](state, { id, progress }) {
    Vue.set(state.progress, id, progress);
  },
  [types.REMOVE_PROGRESS](state, id) {
    Vue.delete(state.progress, id);
  }
};

export const actions = {
  download({ commit, dispatch }, { music, meta, provider }) {
    const url = providers[provider].download(music, meta, progress => {
      progress *= 100;
      if (progress === 100) {
        commit(types.REMOVE_PROGRESS, music.id);
      } else {
        commit(types.SET_PROGRESS, { id: music.id, progress });
      }
    });

    dispatch(
      "musics/add",
      {
        ...music,
        url
      },
      { root: true }
    );

    url.catch(err => {
      console.error(err);
      // TODO: Better way to show message
      alert(`Music ${music.title} download failed`);
      commit(types.REMOVE_PROGRESS, music.id);
      dispatch("musics/deleteMusic", music, { root: true });
    });

    // Indeterminate
    commit(types.SET_PROGRESS, { id: music.id, progress: NaN });
  },
  load() {}
};
