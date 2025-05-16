package com.tasa.service.mock.repo

interface RepoMock {
    val userRepoMock: UserRepoMock
}

class RepoMockImpl() : RepoMock {
    override val userRepoMock: UserRepoMock = UserRepoMock()
}
