package com.alorma.travis.ui.presenter.accounts;

import com.alorma.travis.ui.presenter.utils.retrofit.RetrofitWrapper;
import com.alorma.travisdk.bean.response.GithubAccount;
import com.alorma.travisdk.bean.utils.Credential;
import com.alorma.travisdk.datasource.accounts.AccountsDataSource;
import com.alorma.travisdk.datasource.accounts.cloud.GithubAccountsService;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Response;
import rx.Observable;

public class GithubUserDataSource implements AccountsDataSource {

  private final RetrofitWrapper wrapper;
  private final Credential credential;

  public GithubUserDataSource(RetrofitWrapper wrapper, Credential credential) {
    this.wrapper = wrapper;
    this.credential = credential;
  }

  @Override
  public Observable<List<GithubAccount>> getAccounts() {
    GithubAccountsService githubAccountsService = wrapper.authorization(credential.getGithubToken())
        .accept("application/json")
        .url(credential.getGithubUrl())
        .build()
        .create(GithubAccountsService.class);

    return Observable.create((Observable.OnSubscribe<GithubAccount>) subscriber -> {
      if (!subscriber.isUnsubscribed()) {
        try {
          subscriber.onStart();
          Response<GithubAccount> response = githubAccountsService.me().execute();

          if (response.isSuccess()) {
            subscriber.onNext(response.body());
            subscriber.onCompleted();
          } else {
            subscriber.onError(new Exception(response.errorBody().string()));
          }
          subscriber.onCompleted();
        } catch (Exception e) {
          subscriber.onError(e);
        }
      }
    }).map(githubAccount -> {
      ArrayList<GithubAccount> list = new ArrayList<>();
      list.add(githubAccount);
      return list;
    });
  }

  @Override
  public void save(List<GithubAccount> accounts) {

  }
}
