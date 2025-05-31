package com.example.gamemology.utils;

public class Result<T> {
    private final T data;
    private final String errorMessage;
    private final LoadState state;
    private final boolean hasMore;

    private Result(T data, String errorMessage, LoadState state, boolean hasMore) {
        this.data = data;
        this.errorMessage = errorMessage;
        this.state = state;
        this.hasMore = hasMore;
    }

    public static <T> Result<T> loading() {
        return new Result<>(null, null, LoadState.LOADING, false);
    }

    public static <T> Result<T> loadingMore() {
        return new Result<>(null, null, LoadState.LOADING_MORE, false);
    }

    public static <T> Result<T> success(T data, boolean hasMore) {
        return new Result<>(data, null, LoadState.SUCCESS, hasMore);
    }

    public static <T> Result<T> error(String errorMessage) {
        return new Result<>(null, errorMessage, LoadState.ERROR, false);
    }

    public static <T> Result<T> initial() {
        return new Result<>(null, null, LoadState.INITIAL, false);
    }

    public T getData() {
        return data;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LoadState getState() {
        return state;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public boolean isLoading() {
        return state == LoadState.LOADING || state == LoadState.LOADING_MORE;
    }

    public boolean isSuccess() {
        return state == LoadState.SUCCESS;
    }

    public boolean isError() {
        return state == LoadState.ERROR;
    }
}