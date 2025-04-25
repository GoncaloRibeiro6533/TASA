package pt.isel.models.exclusion

import pt.isel.AppExclusion
import pt.isel.ContactExclusion

data class ExclusionsOutput(
    val nContactExclusions: Int,
    val contactExclusions: List<ContactExclusion>,
    val nAppExclusions: Int,
    val appExclusions: List<AppExclusion>,
)
