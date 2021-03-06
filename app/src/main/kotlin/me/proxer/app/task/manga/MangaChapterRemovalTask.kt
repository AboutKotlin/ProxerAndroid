package me.proxer.app.task.manga

import com.rubengees.ktask.util.WorkerTask
import me.proxer.app.application.MainApplication
import me.proxer.app.application.MainApplication.Companion.globalContext
import me.proxer.app.entity.manga.LocalMangaChapter
import me.proxer.app.task.manga.MangaChapterRemovalTask.MangaChapterRemovalTaskInput
import me.proxer.library.entitiy.info.EntryCore
import java.io.File
import kotlin.concurrent.write

/**
 * @author Ruben Gees
 */
class MangaChapterRemovalTask : WorkerTask<MangaChapterRemovalTaskInput, Unit>() {

    override fun work(input: MangaChapterRemovalTaskInput) {
        MainApplication.mangaDb.removeChapter(input.entry, input.chapter)

        MangaLockHolder.localLock.write {
            File("${globalContext.filesDir}/manga/${input.entry.id}/${input.chapter.id}").deleteRecursively()
        }
    }

    class MangaChapterRemovalTaskInput(val entry: EntryCore, val chapter: LocalMangaChapter)
}
