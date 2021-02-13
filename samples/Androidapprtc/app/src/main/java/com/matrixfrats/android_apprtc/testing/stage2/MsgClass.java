package com.matrixfrats.android_apprtc.testing.stage2;

import androidx.annotation.NonNull;

class MsgClass {

    private String type;
    private Object object;

    public MsgClass(@NonNull String type, @NonNull Object object) {
        this.type = type;
        this.object = object;
    }

    public String getType() {
        return this.type;
    }

    public Object getObject() {
        return this.object;
    }

    public <T> T getDeserializedClass(Class<T> clazz) {
        if (clazz == null) return null;
        if (clazz.isInstance(this.object))
            return clazz.cast(object);
        throw new ClassCastException();
    }
}
