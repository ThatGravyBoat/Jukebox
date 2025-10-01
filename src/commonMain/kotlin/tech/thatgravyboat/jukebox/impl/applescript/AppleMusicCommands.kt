package tech.thatgravyboat.jukebox.impl.applescript

object AppleMusicCommands {

    val POLL_COMMAND = """
    set result to ""
    tell application "music"
    
    set result_progress to round of (get player position) rounding down
    set result_duration to round of (get duration of current track) rounding down
    set result_state to player state
    
    set result_title to name of current track
    set result_artist to artist of current track
    
    set result_shuffle to shuffle enabled
    set result_repeat to song repeat
    set result_volume to round of (get sound volume) rounding down
    
    set result to "" & result_progress & "\n" & result_duration & "\n" & result_state & "\n" & result_title & "\n" & result_artist & "\n" & result_shuffle & "\n" & result_repeat & "\n" & result_volume
    
    end tell
    
    return result
    """.trimIndent()

    val PAUSE_COMMAND = """
    tell application "music"
    pause
    end tell
    """.trimIndent()

    val PLAY_COMMAND = """
    tell application "music"
    play
    end tell
    """.trimIndent()

    val VOLUME_COMMAND = """
    tell application "music"
    set sound volume to {{VOLUME}}
    end tell
    """.trimIndent()

    val PREV_COMMAND = """
    tell application "music"
    if player position > 2 then
        play previous track
        play previous track
    else
        play previous track
    end if
    end tell
    """.trimIndent()

    val NEXT_COMMAND = """
    tell application "music"
    play next track
    end tell
    """.trimIndent()

    val TOGGLE_SHUFFLE_COMMAND = """
    tell application "music"
    set shuffle enabled to (not (get shuffle enabled))
    end tell
    """.trimIndent()

    val TOGGLE_REPEAT_COMMAND = """
    tell application "Music"
    if song repeat is one then
        set song repeat to off
    else if song repeat is off then
        set song repeat to all
    else
        set song repeat to one
    end if
    end tell
    """.trimIndent()
}