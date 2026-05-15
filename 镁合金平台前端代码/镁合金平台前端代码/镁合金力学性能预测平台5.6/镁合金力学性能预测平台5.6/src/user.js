// store/modules/user.js
const state = {
    avatar: '', // 头像 URL
};

const mutations = {
    setAvatar(state, avatar) {
        state.avatar = avatar;
    },
};

const actions = {
    updateAvatar({ commit }, avatar) {
        commit('setAvatar', avatar);
    },
};

export default {
    namespaced: true, // 启用命名空间
    state,
    mutations,
    actions,
};
