package jxpl.scnu.curb.homePage.immediateInformation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jxpl.scnu.curb.R;
import jxpl.scnu.curb.homePage.immediateInformation.informationCreate.InformationCreateActivity;
import jxpl.scnu.curb.utils.ScrollChildSwipeRefreshLayout;
import jxpl.scnu.curb.utils.autoFitRecycler.AutoFitRecyclerView;
import jxpl.scnu.curb.utils.autoFitRecycler.BaseData;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author iri-jwj
 * @version 2
 * update 4.16
 * 将adapter和viewHolder部分的代码转移至新建的类中
 * @see InformationAdapter
 * @see InformationViewHolder
 */
public class InformationFragment extends Fragment implements InformationContract.View {

    @BindView(R.id.info_recycler)
    AutoFitRecyclerView infoRecycler;
    @BindView(R.id.refresh_info_layout)
    ScrollChildSwipeRefreshLayout refreshInfoLayout;
    Unbinder unbinder;

    private InformationContract.Presenter presenter;

    private InformationAdapter m_informationAdapter;

    public InformationFragment() {
        // Required empty public constructor
    }

    @NonNull
    public static InformationFragment newInstance() {
        return new InformationFragment();
    }

    @Override
    public void setPresenter(@NonNull InformationContract.Presenter mPresenter) {
        presenter = checkNotNull(mPresenter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View info = inflater.inflate(R.layout.fragment_information, container, false);
        unbinder = ButterKnife.bind(this, info);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        infoRecycler.setLayoutManager(layoutManager);
        m_informationAdapter = new InformationAdapter(getContext(), infoRecycler);
        infoRecycler.setAdapter(m_informationAdapter);

        //设置默认分割线
        infoRecycler.addItemDecoration(new DividerItemDecoration(
                checkNotNull(getActivity()), DividerItemDecoration.VERTICAL));

        refreshInfoLayout.setColorSchemeColors(
                ContextCompat.getColor(checkNotNull(getActivity()), R.color.toolsBar),
                ContextCompat.getColor(checkNotNull(getActivity()), R.color.colorAccent),
                ContextCompat.getColor(checkNotNull(getActivity()), R.color.colorPrimaryDark)
        );

        // Set the scrolling view in the custom SwipeRefreshLayout.
        refreshInfoLayout.setScrollUpChild(infoRecycler);
        refreshInfoLayout.canChildScrollUp();
        refreshInfoLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                presenter.loadInformation(true);
            }
        });
        setHasOptionsMenu(true);
        return info;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragement_info_add, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_info) {
            checkNotNull(getContext()).startActivity(new Intent(checkNotNull(getActivity()), InformationCreateActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        //TODO 这里偶尔会抛出NullPointerException
        presenter.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("InformationFragment", "onDestroyView: ");
        unbinder.unbind();
    }


    @Override
    public void showInfo(List<ImmediateInformation> immediateInformations) {
        //m_minfoAdapter.replaceInfo(immediateInformations);
        checkNotNull(immediateInformations);
        if (immediateInformations.size() > 0) {
            List<BaseData> lc_baseData = new LinkedList<>();
            Log.d("InformationFragment", "showInfo: " + immediateInformations.get(0).getTitle());
            lc_baseData.addAll(immediateInformations);
            m_informationAdapter.replaceData(lc_baseData);
        }

    }


    @Override
    public void setLoadingIndicator(final boolean active) {
        if (getView() == null)
            return;
        refreshInfoLayout.post(new Runnable() {
            @Override
            public void run() {
                refreshInfoLayout.setRefreshing(active);
            }
        });
    }

    @Override
    public int getImageIdByType(String type) {
        int imageId;
        switch (type) {
            case "EDU":
                imageId = R.drawable.ic_item_edu_info;
                break;
            case "PRA":
                imageId = R.drawable.ic_filter_practice;
                break;
            case "SCHOLAR":
                imageId = R.drawable.ic_filter_scholar;
                break;
            default:
                imageId = 0;
        }
        return imageId;
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void showInformationDetailsUi(UUID id, Context context) {
        Intent intent = new Intent(context, InformationCreateActivity.class);
        intent.putExtra(InformationCreateActivity.INFO_ID, id);
        startActivity(intent);
    }


    @Override
    public void showLoadingError() {
        showLoadingErrorMessage(getString(R.string.info_loading_error));
    }


    @Override
    public void showNoInfo() {
        showLoadingErrorMessage(getString(R.string.info_nothing));
    }


    @Override
    public void showNoNewInfo() {
        showLoadingErrorMessage(getString(R.string.info_no_new_info_error));
    }

    @Override
    public boolean isListShowing() {
        return m_informationAdapter.getItemCount() == 0;
    }

    /**
     * 用于显示各种错误信息
     *
     * @param message 错误信息的内容
     */
    private void showLoadingErrorMessage(String message) {
        View view = checkNotNull(getView());
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }


    @Override
    public List<ImmediateInformation> getCurrentList() {
        return m_informationAdapter.getImmediateInformations();
    }
}
