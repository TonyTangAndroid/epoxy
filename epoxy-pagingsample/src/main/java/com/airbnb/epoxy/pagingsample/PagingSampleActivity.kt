package com.airbnb.epoxy.pagingsample

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.airbnb.epoxy.EpoxyAsyncUtil
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.ModelView
import com.airbnb.epoxy.TextProp
import com.airbnb.epoxy.paging.PagedListEpoxyController
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.coroutines.experimental.bg
import java.util.concurrent.TimeUnit

class PagingSampleActivity : AppCompatActivity() {
    private val compositeDisposable = CompositeDisposable()
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val pagingController = TestController()
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = pagingController.adapter

        val observer = object : DisposableObserver<PagedList<User>>() {
            override fun onNext(pagedList: PagedList<User>) {
                pagingController.submitList(pagedList)

            }

            override fun onError(e: Throwable) {

            }

            override fun onComplete() {
            }
        }

        val viewModel = ViewModelProviders.of(this).get(ActivityViewModel::class.java)
        compositeDisposable.add(viewModel.pagedList.subscribeWith(observer))
    }

    public override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}

class TestController : PagedListEpoxyController<User>(
    modelBuildingHandler = EpoxyAsyncUtil.getAsyncBackgroundHandler()
) {
    override fun buildItemModel(currentPosition: Int, item: User?): EpoxyModel<*> {
        return if (item == null) {
            PagingViewModel_()
                .id(-currentPosition)
                .name("loading ${currentPosition}")
        } else {
            PagingViewModel_()
                .id(item.uid)
                .name("${item.uid}: ${item.firstName} / ${item.lastName}")
        }
    }

    override fun addModels(models: List<EpoxyModel<*>>) {
        pagingView {
            id("header")
            name("showing ${models.size} items")
        }
        super.addModels(models)
    }

    init {
        isDebugLoggingEnabled = true
    }

    override fun onExceptionSwallowed(exception: RuntimeException) {
        throw exception
    }

}

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class PagingView(context: Context) : AppCompatTextView(context) {

    @TextProp
    fun name(name: CharSequence) {
        text = name
    }

}

class ActivityViewModel(app: Application) : AndroidViewModel(app) {
    val db by lazy {
        Room.inMemoryDatabaseBuilder(app, PagingDatabase::class.java).build()
    }

    val pagedList: Observable<PagedList<User>> by lazy {
        RxPagedListBuilder<Int, User>(
            db.userDao().dataSource, 100
        ).setNotifyScheduler(AndroidSchedulers.from(EpoxyAsyncUtil.getAsyncBackgroundHandler().looper))
            .buildObservable()
    }

    init {
        bg {
            (1..3000).map {
                User(it)
            }.let {
                it.groupBy {
                    it.uid / 200
                }.forEach { group ->
                    launch(CommonPool) {
                        delay(group.key.toLong(), TimeUnit.SECONDS)
                        db.userDao().insertAll(group.value)
                    }
                }
            }
        }
    }
}
