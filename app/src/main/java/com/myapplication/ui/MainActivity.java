package com.myapplication.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.karen.myapplication.R;
import com.myapplication.datasource.DataSource;
import com.myapplication.datasource.GenericNetworkErrorHandler;
import com.myapplication.model.LinkInfo;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
/**
 * Created by syang on 10/5/2017.
 */
public class MainActivity extends Activity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String CURRENT_PAGE_KEY = "current_page";
    private static final String SEARCH_PATH_KEY = "search_path";
    private final String REDDIT_TOP_LINK = "https://www.reddit.com/top.json?limit=10";
    private String searchDocPath = "";
    private int currentPageNumber = 0;
    private int pendingPageNum = 0;
    private CompositeDisposable compositeDisposable = null;
    private TextView currentPageView;
    private RecyclerView linksView;
    private LinksAdapter linksAdapter;
    private List<LinkInfo> linkData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        linksView = (RecyclerView) findViewById(R.id.links_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linksView.setLayoutManager(linearLayoutManager);
        linksAdapter = new LinksAdapter(linkData);
        linksView.setAdapter(linksAdapter);

        Button button = (Button) findViewById(R.id.prev_page);
        button.setOnClickListener(this);
        button = (Button) findViewById(R.id.next_page);
        button.setOnClickListener(this);

        currentPageView = (TextView) findViewById(R.id.current_page_num);

        if (savedInstanceState != null && savedInstanceState.getInt(CURRENT_PAGE_KEY, 0) > 0) {
            searchDocPath = savedInstanceState.getString(SEARCH_PATH_KEY, "");
            retrieveTopInfo(savedInstanceState.getInt(CURRENT_PAGE_KEY));
        } else {
            retrieveTopInfo(1);
        }
    }

    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        // Only saving these two parameters, other data will retrieve from the clouds again
        state.putInt(CURRENT_PAGE_KEY, currentPageNumber);
        state.putString(SEARCH_PATH_KEY, searchDocPath);
        // TODO: We can also save linkData and the current scroll position of the recycler view
        // and restore them when the activity is recreated after screen rotation
    }

    private void updateCurrentPageNum(int pageNum) {
        currentPageView.setText(String.valueOf(pageNum));
        this.currentPageNumber = pageNum;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.prev_page: {
                retrieveTopInfo(currentPageNumber - 1);
                break;
            }
            case R.id.next_page: {
                retrieveTopInfo(currentPageNumber + 1);
                break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
    }

    private void retrieveTopInfo(int pageNumber) {
        if (pageNumber <= 0) {
            pageNumber = 1;
        } else if (pageNumber > 5) {
            pageNumber = 5;
        }
        if (pageNumber == currentPageNumber || pageNumber == pendingPageNum) return;

        // Pagination
        if (currentPageNumber != 0 && !linkData.isEmpty()) {
            if (pageNumber < currentPageNumber) {
                searchDocPath = "&before=t3_" + linkData.get(0).getId();
            } else {
                searchDocPath = "&after=t3_" + linkData.get(linkData.size() - 1).getId();
            }
        }

        Log.d(TAG, "Page: " + pageNumber);

        if (compositeDisposable != null) {
            compositeDisposable.dispose();
            compositeDisposable = null;
        }
        if (compositeDisposable == null) {
            compositeDisposable = new CompositeDisposable();
        }
        pendingPageNum = pageNumber;
        Observable<List<LinkInfo>> observable = DataSource.getInstance().getRedditLinks(REDDIT_TOP_LINK + searchDocPath);
        Disposable disposable = observable.subscribe(new Consumer<List<LinkInfo>>() {
                    @Override
                    public void accept(List<LinkInfo> linkInfos) throws Exception {
                        linkData.clear();
                        linkData.addAll(linkInfos);
                        linksAdapter.notifyDataSetChanged();
                        updateCurrentPageNum(pendingPageNum);
                    }
                }, new GenericNetworkErrorHandler());
        compositeDisposable.add(disposable);
    }
}
