package com.ldtteam.fabric.event.level;

import com.ldtteam.fabric.event.bus.Event;
import com.ldtteam.fabric.event.bus.ICancellableEvent;

/** Fabric port of NeoForge's event of the same name (see com.ldtteam.fabric.event.EventBridge for where it fires). */
public class NoteBlockEvent extends Event
{

    public NoteBlockEvent()
    {
    }

    /** NeoForge's note names, in note-block order (0 = F#3 ... 24 = F#5). */
    public enum Note
    {
        F_SHARP, G, G_SHARP, A, A_SHARP, B, C, C_SHARP, D, D_SHARP, E, F;

        private static final Note[] VALUES = values();

        public static Note fromId(final int id)
        {
            return VALUES[id % 12];
        }
    }
}
