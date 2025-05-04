package pt.isel

import org.jdbi.v3.core.Handle
import pt.isel.location.LocationRepository

class JdbiLocationRepository(
    private val handle: Handle,
) : LocationRepository {
    override fun create(
        name: String,
        latitude: Double,
        longitude: Double,
        radius: Double,
        user: User,
    ): Location {
        val id =
            handle.createUpdate(
                """
                INSERT INTO ps.Location (name, latitude, longitude, radius, user_id) values 
                (:name, :latitude, :longitude, :radius, :userId)
                """.trimIndent(),
            )
                .bind("name", name)
                .bind("latitude", latitude)
                .bind("longitude", longitude)
                .bind("radius", radius)
                .bind("userId", user.id)
                .executeAndReturnGeneratedKeys().mapTo(Int::class.java).one()
        return Location(
            id = id,
            name = name,
            latitude = latitude,
            longitude = longitude,
            radius = radius,
        )
    }

    override fun findById(id: Int): Location? {
        return handle.createQuery(
            """
            SELECT id, name, latitude, longitude, radius FROM ps.LOCATION WHERE id = :id
            """.trimIndent(),
        )
            .bind("id", id)
            .mapTo(Location::class.java)
            .findOne()
            .orElse(null)
    }

    override fun findAll(): List<Location> {
        return handle.createQuery("SELECT id, name, latitude, longitude, radius FROM ps.LOCATION")
            .mapTo(Location::class.java)
            .list()
    }

    override fun findByUserId(user: User): List<Location> {
        return handle.createQuery(
            """
            SELECT id, name, latitude, longitude, radius FROM ps.LOCATION WHERE user_id = :userId
            """.trimIndent(),
        )
            .bind("userId", user.id)
            .mapTo(Location::class.java)
            .list()
    }

    override fun updateRadius(
        location: Location,
        radius: Double,
    ): Location {
        handle.createUpdate(
            """
            UPDATE ps.LOCATION SET radius = :radius WHERE id = :id
            """.trimIndent(),
        )
            .bind("radius", radius)
            .bind("id", location.id)
            .execute()
        return location.copy(radius = radius)
    }

    override fun updateName(
        location: Location,
        name: String,
    ): Location {
        handle.createUpdate(
            """
            UPDATE ps.LOCATION set name = :name WHERE id = :id
            """.trimIndent(),
        )
            .bind("name", name)
            .bind("id", location.id)
            .execute()
        return location.copy(name = name)
    }

    override fun updateCoordinates(
        location: Location,
        latitude: Double,
        longitude: Double,
    ): Location {
        handle.createUpdate(
            """
            UPDATE ps.LOCATION SET latitude = :latitude, longitude = :longitude WHERE id = :id
            """.trimIndent(),
        )
            .bind("latitude", latitude)
            .bind("longitude", longitude)
            .bind("id", location.id)
            .execute()
        return location.copy(latitude = latitude, longitude = longitude)
    }

    override fun delete(location: Location): Boolean {
        return handle.createUpdate(
            """
            DELETE FROM ps.LOCATION WHERE id = :id
            """.trimIndent(),
        ).bind("id", location.id).execute() > 0
    }

    override fun clear() {
        handle.createUpdate(
            """
            DELETE FROM ps.LOCATION
            """.trimIndent(),
        )
    }
}
