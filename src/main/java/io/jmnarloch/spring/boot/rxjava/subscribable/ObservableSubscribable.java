/**
 * Copyright (c) 2015-2016 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jmnarloch.spring.boot.rxjava.subscribable;

import rx.Observable;
import rx.Scheduler;

/**
 * The wrapper around {@link Observable} allowing for subscribing to specific {@link Scheduler}
 *
 * @author Jakub Narloch
 * @see Observable
 * @see Scheduler
 * @see Subscribable
 */
class ObservableSubscribable implements Subscribable {

    private final Observable delegate;

    public ObservableSubscribable(Observable delegate) {
        this.delegate = delegate;
    }

    @Override
    public Subscribable subscribeOn(Scheduler scheduler) {
        return new ObservableSubscribable(delegate.subscribeOn(scheduler));
    }

    @Override
    public Object unwrap() {
        return delegate;
    }
}