package com.levibostian.androidblanky

import android.app.Instrumentation
import android.content.Intent
import android.content.SharedPreferences
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.*
import com.levibostian.androidblanky.view.ui.activity.MainActivity
import android.support.test.rule.ActivityTestRule
import org.junit.Rule
import android.support.test.runner.AndroidJUnit4
import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.levibostian.androidblanky.service.GitHubService
import com.levibostian.androidblanky.service.datasource.GitHubUsernameDataSource
import com.levibostian.androidblanky.service.module.DataSourceModule
import com.levibostian.androidblanky.service.module.RepositoryModule
import com.levibostian.androidblanky.service.module.ServiceModule
import com.levibostian.androidblanky.view.ui.ApplicationComponent
import com.levibostian.androidblanky.view.ui.MainApplication
import com.levibostian.androidblanky.viewmodel.module.ManagerModule
import com.levibostian.androidblanky.viewmodel.module.ViewModelModule
import io.reactivex.Observable
import org.junit.Test
import org.junit.runner.RunWith
import org.hamcrest.MatcherAssert.*
import org.junit.Before
import retrofit2.Retrofit
import tools.fastlane.screengrab.locale.LocaleTestRule
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy
import javax.inject.Inject
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.hamcrest.Matchers
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.levibostian.androidblanky.service.model.OwnerModel
import com.levibostian.androidblanky.service.model.RepoModel
import com.levibostian.androidblanky.service.model.SharedPrefersKeys
import com.levibostian.androidblanky.service.wrapper.RxSharedPreferencesWrapper
import com.levibostian.androidblanky.view.ui.TestMainApplication
import com.levibostian.androidblanky.view.ui.fragment.MainFragment
import com.levibostian.androidblanky.viewmodel.ReposViewModel
import com.levibostian.androidblanky.viewmodel.ViewModelFactory
import com.levibostian.androidblanky.viewmodel.ViewModelProviderWrapper
import io.reactivex.rxkotlin.toSingle
import org.junit.BeforeClass
import org.mockito.*
import retrofit2.Response
import retrofit2.adapter.rxjava2.Result

@RunWith(AndroidJUnit4::class)
open class DummyTest {

    @Inject lateinit var gitHubService: GitHubService
    @Inject lateinit var sharedPrefs: SharedPreferences
    @Inject lateinit var rxSharedPrefsWrapper: RxSharedPreferencesWrapper

    @Mock private lateinit var preference: Preference<String>

    @get:Rule open val main = ActivityTestRule(MainActivity::class.java, false, false)
    @get:Rule open val localeTestRule = LocaleTestRule() // fastlane can switch locales to take screenshots and test.

    private val repo1 = RepoModel("name1", "description1", OwnerModel("login1"))
    private val repo2 = RepoModel("name2", "description2", OwnerModel("login2"))
    private val repo3 = RepoModel("name3", "description3", OwnerModel("login3"))

    private lateinit var application: TestMainApplication

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        val instrumentation = InstrumentationRegistry.getInstrumentation()
        application = instrumentation.targetContext.applicationContext as TestMainApplication
        (application.component as MockApplicationComponent).inject(this)
    }

    private fun launchActivity() {
        main.launchActivity(null)
    }

    @Test fun noneOfTheThings() {
        `when`(rxSharedPrefsWrapper.getString(com.nhaarman.mockito_kotlin.eq(SharedPrefersKeys.gitHubUsernameKey))).thenReturn(preference)
        `when`(preference.asObservable()).thenReturn(Observable.create { it.onNext("levibostian") })
        `when`(gitHubService.getRepos(ArgumentMatchers.anyString())).thenReturn(Result.response(Response.success(listOf(repo1, repo2, repo3))).toSingle())

        launchActivity()

        Screengrab.screenshot("initial_state")
        onView(withId(R.id.fragment_main_username_textview))
                .check(ViewAssertions.matches(ViewMatchers.withText("levibostian")))
    }

}