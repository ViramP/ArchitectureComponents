package com.android.example.github.repository

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.android.example.github.AppExecutors
import com.android.example.github.api.*
import com.android.example.github.vo.Resource

/**
 * Created by Viram Purohit on 6/6/2019.
 */
abstract class NetworkOnlyBoundResource<ResultType, RequestType> @MainThread
internal constructor(private val appExecutors: AppExecutors) {

    private val result = MediatorLiveData<Resource<RequestType>>()

    init {
        result.value = Resource.loading(null)
        // LiveData<ResultType> dbSource = loadFromDb();

        val apiResponse = createCall()
        // result.addSource(apiResponse, newData -> result.setValue(Resource.loading(newData.body)));
        result.addSource(apiResponse) { response ->

            when (response) {
                is ApiSuccessResponsee -> {
                    appExecutors.mainThread().execute {
                        // reload from disk whatever we had
                        result.value = Resource.success(response.body)
                        asLiveData()
                    }
                }

                is ApiEmptyResponse -> {
                    appExecutors.mainThread().execute {
                        // reload from disk whatever we had
                        result.value = Resource.success(null)
                        asLiveData()
                    }
                }

                is ApiErrorResponse -> {

                    result.value = Resource.error(response.errorMessage, null);
                    onFetchFailed()
                }

            }

        }

    }


    protected fun onFetchFailed() {}

    fun asLiveData(): LiveData<Resource<RequestType>> {
        return result
    }

    @MainThread
    protected abstract fun createCall(): LiveData<ApiResponse<RequestType>>
}
