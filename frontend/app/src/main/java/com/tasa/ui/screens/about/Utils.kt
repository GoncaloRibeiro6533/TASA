package com.tasa.ui.screens.about

import android.net.Uri
import androidx.annotation.DrawableRes
import com.tasa.R

/**
 * Used to represent information about a social network in the about screen
 * @param imageId the id of the image to be displayed
 */
data class CreatorInfo(
    val name: String,
    @DrawableRes val imageId: Int,
    val socials: List<SocialInfo>,
    val email: String,
)

data class SocialInfo(
    val link: Uri,
    @DrawableRes val imageId: Int,
)

fun socialsDefault(githubName: String) =
    listOf(
        SocialInfo(
            link = Uri.parse("https://github.com/$githubName"),
            imageId = R.drawable.ic_github,
        ),
    )

val defaultAuthors =
    listOf(
        CreatorInfo(
            name = "Gonçalo Ribeiro",
            imageId = R.drawable.ic_user_img,
            socials = socialsDefault("GoncaloRibeiro6533"),
            email = "A48305@alunos.isel.pt",
        ),
        CreatorInfo(
            name = "João Marques",
            imageId = R.drawable.ic_user_img,
            socials = socialsDefault("joaorvm"),
            email = "A48297@alunos.isel.pt",
        ),
    )
