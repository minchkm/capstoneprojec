package com.project.gudasi;

import androidx.annotation.DrawableRes;

public final class ServiceIconMapper {
    private ServiceIconMapper() {}

    @DrawableRes
    public static int iconOfCanonical(String canonical) {
        if (canonical == null) return R.drawable.ic_default_service;
        String n = canonical.trim().toLowerCase();
        if (n.contains("netflix") || n.contains("넷플릭스")) return R.drawable.ic_netflix;
        if (n.contains("youtube") || n.contains("유튜브"))   return R.drawable.ic_youtube_music;
        if (n.contains("melon")   || n.contains("멜론"))     return R.drawable.ic_melon;
        if (n.contains("spotify") || n.contains("스포티파이")) return R.drawable.ic_spotify;
        if (n.contains("watcha")  || n.contains("왓챠"))     return R.drawable.ic_watcha;
        if (n.contains("coupang") || n.contains("쿠팡") || n.contains("와우")) return R.drawable.ic_coupang;
        return R.drawable.ic_default_service;
    }
}
