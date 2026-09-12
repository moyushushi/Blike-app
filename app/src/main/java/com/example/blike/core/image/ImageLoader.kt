package com.example.blike.core.image

class ImageLoader(
    private val interceptors: List<ImageInterceptor> = emptyList()
) {
    suspend fun load(request: ImageRequest): ImageResult {
        return RealChain(request, interceptors, 0).proceed(request)
    }

    private class RealChain(
        override val request: ImageRequest,
        private val interceptors: List<ImageInterceptor>,
        private val index: Int
    ) : ImageChain {

         override suspend fun proceed(request: ImageRequest): ImageResult {
            if (index >= interceptors.size) {
                return ImageResult.Error(
                    UnsupportedOperationException("未实现下载解码")
                )
            }
            return interceptors[index].intercept(
                RealChain(request, interceptors, index + 1)
            )
        }

    }
}