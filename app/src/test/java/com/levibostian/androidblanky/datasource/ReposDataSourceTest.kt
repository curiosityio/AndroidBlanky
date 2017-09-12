package com.levibostian.androidblanky.datasource

import android.annotation.SuppressLint
import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.content.SharedPreferences
import com.levibostian.androidblanky.service.GitHubService
import com.levibostian.androidblanky.service.RealmInstanceWrapper
import com.levibostian.androidblanky.service.datasource.ReposDataSource
import com.levibostian.androidblanky.service.error.nonfatal.UserErrorException
import com.levibostian.androidblanky.service.model.OwnerModel
import com.levibostian.androidblanky.service.model.RepoModel
import com.levibostian.androidblanky.service.model.SharedPrefersKeys
import io.reactivex.Single
import io.realm.Realm
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.Response
import retrofit2.adapter.rxjava2.Result
import com.levibostian.androidblanky.RxImmediateSchedulerRule
import io.realm.RealmAsyncTask
import org.junit.ClassRule
import org.mockito.Mockito


@RunWith(MockitoJUnitRunner::class)
class ReposDataSourceTest {

    @get:Rule var instantTaskExecutorRule = InstantTaskExecutorRule()
    @get:Rule val schedulers = RxImmediateSchedulerRule()

    @Mock private lateinit var sharedPreferences: SharedPreferences
    @Mock private lateinit var sharedPreferencesEditor: SharedPreferences.Editor
    @Mock private lateinit var githubService: GitHubService
    @Mock private lateinit var realmWrapper: RealmInstanceWrapper
    @Mock private lateinit var realm: Realm
    @Mock private lateinit var result: Result<List<RepoModel>>
    @Mock private lateinit var response: Response<List<RepoModel>>
    @Mock private lateinit var realmAsyncTask: RealmAsyncTask

    val repo1 = RepoModel("name1", "desc1", OwnerModel("login1"))
    val repo2 = RepoModel("name2", "desc2", OwnerModel("login2"))
    val repo3 = RepoModel("name3", "desc3", OwnerModel("login3"))

    // @Captor private lateinit var userArgumentCaptor: ArgumentCaptor<RepoRepository>

    private lateinit var reposDataSource: ReposDataSource

    @Before fun setUp() {
        `when`(realmWrapper.getDefault()).thenReturn(realm)
        reposDataSource = ReposDataSource(sharedPreferences, githubService, realmWrapper)
    }

    @Test fun fetchNewData_didThrow404() {
        `when`(githubService.getRepos(ArgumentMatchers.anyString())).thenReturn(Single.just(result))
        `when`(result.isError).thenReturn(false)
        `when`(result.response()).thenReturn(response)
        `when`(response.isSuccessful).thenReturn(false)
        `when`(response.code()).thenReturn(404)

        reposDataSource.fetchNewData(ReposDataSource.FetchNewDataRequirements("username"))
                .test()
                .assertError(UserErrorException::class.java)
    }

    @SuppressLint("CommitPrefEdits")
    @Test fun fetchNewData_fetchNewRepos() {
        `when`(githubService.getRepos(ArgumentMatchers.anyString())).thenReturn(Single.just(result))
        `when`(result.isError).thenReturn(false)
        `when`(result.response()).thenReturn(response)
        `when`(response.isSuccessful).thenReturn(true)
        `when`(response.body()).thenReturn(listOf(repo1, repo2, repo3))

        `when`(sharedPreferences.edit()).thenReturn(sharedPreferencesEditor)
        `when`(sharedPreferencesEditor.putLong(ArgumentMatchers.anyString(), ArgumentMatchers.anyLong())).thenReturn(sharedPreferencesEditor)
        `when`(realm.executeTransaction(ArgumentMatchers.any(Realm.Transaction::class.java))).thenCallRealMethod()

        reposDataSource.fetchNewData(ReposDataSource.FetchNewDataRequirements("username"))
                .test()
                .assertNoErrors()

        verify(realm).delete(RepoModel::class.java)
        verify(realm).insert(listOf(repo1, repo2, repo3))

        verify(sharedPreferencesEditor).putLong(eq(SharedPrefersKeys.lastTimeReposFetchedKey), ArgumentMatchers.longThat { it > 0 })
        verify(sharedPreferencesEditor).commit()
    }

    @Test fun cleanup_clearAndCleanup() {
        reposDataSource.cleanup()

        verify(realm).close()
    }

}