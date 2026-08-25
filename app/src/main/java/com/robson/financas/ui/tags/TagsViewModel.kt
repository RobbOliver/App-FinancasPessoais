package com.robson.financas.ui.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.local.entity.TagEntity
import com.robson.financas.data.repository.TagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TagsViewModel @Inject constructor(
    private val tagRepository: TagRepository,
) : ViewModel() {

    val tags: StateFlow<List<TagEntity>> = tagRepository
        .observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveTag(id: Long?, name: String, colorHex: String) {
        viewModelScope.launch {
            if (id != null) {
                tagRepository.update(TagEntity(id = id, name = name, colorHex = colorHex))
            } else {
                tagRepository.create(TagEntity(name = name, colorHex = colorHex))
            }
        }
    }

    fun deleteTag(tag: TagEntity) {
        viewModelScope.launch { tagRepository.delete(tag) }
    }
}
