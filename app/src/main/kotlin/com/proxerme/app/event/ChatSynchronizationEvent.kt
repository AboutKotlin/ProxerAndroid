package com.proxerme.app.event

import com.proxerme.app.entitiy.LocalConference
import com.proxerme.app.entitiy.LocalMessage

/**
 * TODO: Describe class
 *
 * @author Ruben Gees
 */
class ChatSynchronizationEvent(val newEntryMap: Map<LocalConference, List<LocalMessage>>)