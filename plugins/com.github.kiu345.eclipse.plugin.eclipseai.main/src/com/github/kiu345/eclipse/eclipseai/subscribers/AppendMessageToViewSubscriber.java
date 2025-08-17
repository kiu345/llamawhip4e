package com.github.kiu345.eclipse.eclipseai.subscribers;

import java.util.Objects;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscription;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.eclipse.core.runtime.ILog;
import org.eclipse.e4.core.di.annotations.Creatable;

import com.github.kiu345.eclipse.eclipseai.model.ChatMessage;
import com.github.kiu345.eclipse.eclipseai.model.Incoming;
import com.github.kiu345.eclipse.eclipseai.model.Incoming.Type;
import com.github.kiu345.eclipse.eclipseai.part.ChatPresenter;

@Creatable
@Singleton
public class AppendMessageToViewSubscriber implements Flow.Subscriber<Incoming> {
    @Inject
    private ILog logger;

    private Flow.Subscription subscription;

    private ChatMessage message;
    private ChatPresenter presenter;

    public AppendMessageToViewSubscriber() {
    }

    public void setPresenter(ChatPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        Objects.requireNonNull(presenter);
        this.subscription = subscription;
        message = presenter.beginMessageFromAssistant();
        subscription.request(1);
    }

    @Override
    public void onNext(Incoming item) {
        Objects.requireNonNull(presenter);
        Objects.requireNonNull(message);
        Objects.requireNonNull(subscription);
        if (Type.ERROR.equals(item.type())) {
            message.setContent(item.payload());
            presenter.onError(message);
        }
        else {
            if (item.append()) {
                message.append(item.payload());
            }
            else {
                message.setContent(item.payload());
            }
            presenter.updateMessageFromAssistant(message);
        }
        subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
        message = null;
        logger.error(throwable.getMessage(), throwable);
    }

    @Override
    public void onComplete() {
        Objects.requireNonNull(presenter);
        message = null;
        subscription = null;
        presenter.endMessageFromAssistant();
        subscription.request(1);
    }

}
