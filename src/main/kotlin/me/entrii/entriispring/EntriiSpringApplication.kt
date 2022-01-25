@file:Suppress("unused")

package me.entrii.entriispring

import java.util.UUID
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@SpringBootApplication
class EntriiSpringApplication(val entriiRepository: EntriiRepository) : ErrorController {

    @GetMapping(
        "/"
    )
    fun mainPage() {

    }

    @GetMapping(
        "/search",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun search(@RequestParam(name = "query", defaultValue = "no_query") id: String): ResponseEntity<List<Entrii>> {
        if (id == "no_query") return ResponseEntity(HttpStatus.BAD_REQUEST)
        val list = entriiRepository.findEntriisByTagsContaining(id)
        return ResponseEntity(list, HttpStatus.OK)
    }

    @GetMapping(
        "/entrii/{id}",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getByID(@PathVariable id: String): ResponseEntity<Entrii> {
        val identifier = UUID.fromString(id) ?: return ResponseEntity(HttpStatus.BAD_REQUEST)
        val optionalEntrii = entriiRepository.findById(identifier)
        return if (optionalEntrii.isEmpty) ResponseEntity(HttpStatus.NOT_FOUND) else ResponseEntity(
            optionalEntrii.get(),
            HttpStatus.OK
        )
    }

    @PostMapping(
        "/like",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun addLike(@RequestBody identifier: UUID): ResponseEntity<Entrii> {
        val entrii = entriiRepository.findById(identifier)
        if (entrii.isEmpty) return ResponseEntity(HttpStatus.NOT_FOUND)
        entrii.get().likes++
        return ResponseEntity(entrii.get(), HttpStatus.OK)
    }

    @PostMapping(
        "/make",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun make(@RequestBody entriiCreation: EntriiCreation): ResponseEntity<Entrii> {
        val tags = entriiCreation.content.split(" ").filter { it.startsWith("#") }
        val entrii = Entrii(UUID.randomUUID(), entriiCreation.title, entriiCreation.content, tags)
        entriiRepository.save(entrii)
        return ResponseEntity(entrii, HttpStatus.OK)
    }
}

interface EntriiRepository : MongoRepository<Entrii, UUID> {
    fun findEntriisByTagsContaining(string: String): List<Entrii>
    fun findEntriisByTitleContains(title: String): List<Entrii>
    fun findEntriisByContentContains(title: String): List<Entrii>
}

data class EntriiCreation(
    val title: String,
    val content: String
)

data class Entrii(
    val identifier: UUID,
    val title: String,
    val content: String,
    val tags: List<String>,
    var likes: Int = 0
)

fun main(args: Array<String>) {
    runApplication<EntriiSpringApplication>(*args)
}