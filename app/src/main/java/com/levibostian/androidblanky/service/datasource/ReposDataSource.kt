package com.levibostian.androidblanky.service.datasource

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import com.levibostian.androidblanky.service.GitHubService
import com.levibostian.androidblanky.service.db.manager.RealmInstanceManager
import com.levibostian.androidblanky.service.dao.repoDao
import com.levibostian.androidblanky.service.error.fatal.HttpUnhandledStatusCodeException
import com.levibostian.androidblanky.service.error.nonfatal.RecoverableBadNetworkConnectionException
import com.levibostian.androidblanky.service.error.nonfatal.UserErrorException
import com.levibostian.androidblanky.service.model.RepoModel
import com.levibostian.androidblanky.service.model.SharedPrefersKeys
import com.levibostian.androidblanky.service.wrapper.LooperWrapper
import io.reactivex.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.RealmResults
import java.io.IOException
import java.util.*

open class ReposDataSource(override val sharedPreferences: SharedPreferences,
                           private val gitHubService: GitHubService,
                           private val realmManager: RealmInstanceManager) : DataSource<RealmResults<RepoModel>, ReposDataSource.FetchNewDataRequirements, List<RepoModel>>(sharedPreferences) {

    private var uiRealm: Realm = realmManager.getDefault()

    override fun fetchFreshDataOrFail(requirements: FetchNewDataRequirements): Single<List<RepoModel>> {
        return gitHubService.getRepos(requirements.githubUsername)
                .mapApiCallResult { statusCode ->
                    if (statusCode == 404) throw UserErrorException("The username you entered does not exist in GitHub. Try another username.")
                }
    }

    override fun deleteData(): Completable {
        return Completable.fromCallable {
            val realm = realmManager.getDefault()
            realm.executeTransaction {
                realm.delete(RepoModel::class.java)
            }
            realm.close()
        }.subscribeOn(Schedulers.io())
    }

    override fun lastTimeFreshDataFetchedKey(): String = SharedPrefersKeys.lastTimeReposFetchedKey

    override fun saveData(data: List<RepoModel>): Completable {
        return Completable.fromCallable {
            val realm = realmManager.getDefault()
            realm.executeTransaction {
                realm.delete(RepoModel::class.java)
                realm.insert(data)
            }
            realm.close()
        }.subscribeOn(Schedulers.io())
    }

    override fun getData(): Observable<RealmResults<RepoModel>> {
        return uiRealm.repoDao().getReposUi().toObservable().subscribeOn(AndroidSchedulers.mainThread())
    }

    override fun cleanup() {
        uiRealm.close()
    }

    open class FetchNewDataRequirements(val githubUsername: String)

}