package mysql;

import java.util.Observable;
import java.util.Observer;

public abstract class DBObserverMapCache<T, U extends Observable> extends DBMapCache<T, U> implements Observer {

    private final DBObserverMapCache<T, U> INSTANCE = this;

    protected DBObserverMapCache() {
    }

    @Override
    protected U process(T t) throws Exception {
        U u = DBObserverMapCache.this.load(t);
        u.addObserver(INSTANCE);
        return u;
    }

    protected abstract void save(U u);

    @Override
    public void update(Observable o, Object arg) {
        save((U) o);
    }

}
