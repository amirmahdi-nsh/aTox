package ltd.evilcorp.core.repository

import androidx.lifecycle.LiveData
import ltd.evilcorp.core.db.ContactDao
import ltd.evilcorp.core.vo.Contact
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepository @Inject internal constructor(
    private val contactDao: ContactDao
) {
    fun exists(publicKey: String): Boolean =
        contactDao.exists(publicKey)

    fun add(contact: Contact) =
        contactDao.save(contact)

    fun update(contact: Contact) =
        contactDao.update(contact)

    fun delete(contact: Contact) =
        contactDao.delete(contact)

    fun get(publicKey: String): LiveData<Contact> =
        contactDao.load(publicKey)

    fun getAll(): LiveData<List<Contact>> =
        contactDao.loadAll()

    fun resetTransientData() =
        contactDao.resetTransientData()

    fun setAvatarUri(publicKey: String, uri: String) =
        contactDao.setAvatarUri(publicKey, uri)
}