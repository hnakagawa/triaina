package triaina.injector.service;

import proton.inject.service.ProtonIntentService;

public abstract class TriainaIntentService extends ProtonIntentService {

    public TriainaIntentService(String name) {
        super(name);
    }
}
