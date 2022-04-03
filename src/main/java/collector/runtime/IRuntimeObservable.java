package collector.runtime;

public interface IRuntimeObservable {

    void registerObserver(IRuntimeObserver o);

    void unregisterObserver(IRuntimeObserver o);

}
