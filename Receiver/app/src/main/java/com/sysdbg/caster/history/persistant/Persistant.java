package com.sysdbg.caster.history.persistant;

import com.sysdbg.caster.history.HistoryItem;

import java.util.Collection;
import java.util.List;

/**
 * Created by crady on 3/5/2016.
 */
public interface Persistant {
    void save(Collection<HistoryItem> item);
    List<HistoryItem> load();
}

