package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 推荐帖子
 * 社区推荐列表中的帖子，用于完成浏览、点赞、分享等金币任务
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecommendPost {
    /** 帖子ID */
    private String postId;
    /** 当前用户对该帖子的操作状态 */
    private SelfOperation selfOperation;

    public RecommendPost() {
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public SelfOperation getSelfOperation() {
        return selfOperation;
    }

    public void setSelfOperation(SelfOperation selfOperation) {
        this.selfOperation = selfOperation;
    }

    /**
     * 用户对帖子的操作状态
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SelfOperation {
        /** 是否已点赞 */
        private Boolean liked;

        public SelfOperation() {
        }

        public Boolean getLiked() {
            return liked;
        }

        public void setLiked(Boolean liked) {
            this.liked = liked;
        }
    }
}
