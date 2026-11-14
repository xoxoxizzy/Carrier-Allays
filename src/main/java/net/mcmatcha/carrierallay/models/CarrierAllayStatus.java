package net.mcmatcha.carrierallay.models;

import java.util.HashMap;
import java.util.Map;

public enum CarrierAllayStatus {

    WAITING(1),
    DELIVERY(2),
    RETURNING(3),
    COMPLETE(4);

    private final int code;
    private static final Map<Integer, CarrierAllayStatus> CODES_MAP = new HashMap<>();

    static {
        for (CarrierAllayStatus s : values()) {
            CODES_MAP.put(s.code, s);
        }
    }

    private CarrierAllayStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public static CarrierAllayStatus fromCode(int code) {
        CarrierAllayStatus status = CODES_MAP.get(code);
        if (status == null) {
            throw new IllegalArgumentException("Invalid Status Code");
        }
        return status;
    }

}
