package com.telecommande.core

import com.telecommande.core.pairing.PairingMessageManager
import com.telecommande.core.pairing.Pairingmessage
import com.telecommande.core.remote.RemoteMessageManager
import com.telecommande.core.remote.Remotemessage
import com.telecommande.core.util.Utils
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProtocolCoreTest {

    @Test
    fun `big endian int conversion round trips`() {
        val values = listOf(
            0,
            1,
            255,
            256,
            0x12345678,
            Int.MAX_VALUE,
            Int.MIN_VALUE,
            -1
        )

        values.forEach { value ->
            val bytes = Utils.intToBigEndianIntBytes(value)
            assertEquals(4, bytes.size)
            assertEquals(value.toUInt().toLong(), Utils.intBigEndianBytesToLong(bytes))
        }
    }

    @Test
    fun `hex conversion round trips and preserves leading zeroes`() {
        val source = byteArrayOf(0x00, 0x01, 0x0f, 0x10, 0x7f, 0x80.toByte(), 0xff.toByte())

        val hex = Utils.bytesToHexString(source)

        assertEquals("00010f107f80ff", hex)
        assertArrayEquals(source, Utils.hexStringToBytes(hex))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `hex conversion rejects odd length`() {
        Utils.hexStringToBytes("abc")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `hex conversion rejects non hexadecimal characters`() {
        Utils.hexStringToBytes("zz")
    }

    @Test
    fun `remote key command contains expected key and direction`() {
        val encoded = RemoteMessageManager().createKeyCommand(
            Remotemessage.RemoteKeyCode.KEYCODE_VOLUME_UP,
            Remotemessage.RemoteDirection.SHORT
        )

        val message = Remotemessage.RemoteMessage.parseFrom(payload(encoded))

        assertTrue(message.hasRemoteKeyInject())
        assertEquals(Remotemessage.RemoteKeyCode.KEYCODE_VOLUME_UP, message.remoteKeyInject.keyCode)
        assertEquals(Remotemessage.RemoteDirection.SHORT, message.remoteKeyInject.direction)
    }

    @Test
    fun `remote app launch contains requested link`() {
        val appLink = "https://www.youtube.com"
        val encoded = RemoteMessageManager().createAppLinkLaunchRequest(appLink)

        val message = Remotemessage.RemoteMessage.parseFrom(payload(encoded))

        assertTrue(message.hasRemoteAppLinkLaunchRequest())
        assertEquals(appLink, message.remoteAppLinkLaunchRequest.appLink)
    }

    @Test
    fun `remote configure contains client identity`() {
        val encoded = RemoteMessageManager().createRemoteConfigure(
            code = 622,
            model = "TestModel",
            vendor = "TestVendor",
            unknown1 = 1,
            unknown2 = "1"
        )

        val message = Remotemessage.RemoteMessage.parseFrom(payload(encoded))

        assertTrue(message.hasRemoteConfigure())
        assertEquals(622, message.remoteConfigure.code1)
        assertEquals("TestModel", message.remoteConfigure.deviceInfo.model)
        assertEquals("TestVendor", message.remoteConfigure.deviceInfo.vendor)
        assertEquals("androidtv-remote", message.remoteConfigure.deviceInfo.packageName)
    }

    @Test
    fun `pairing request uses protocol version two and expected identity`() {
        val encoded = PairingMessageManager().createPairingMessage(
            clientName = "Phone",
            serviceName = "androidtvremote"
        )

        val message = Pairingmessage.PairingMessage.parseFrom(payload(encoded))

        assertTrue(message.hasPairingRequest())
        assertEquals(2, message.protocolVersion)
        assertEquals(Pairingmessage.PairingMessage.Status.STATUS_OK, message.status)
        assertEquals("Phone", message.pairingRequest.clientName)
        assertEquals("androidtvremote", message.pairingRequest.serviceName)
    }

    @Test
    fun `pairing option requests six digit hexadecimal input`() {
        val encoded = PairingMessageManager().createPairingOption()
        val message = Pairingmessage.PairingMessage.parseFrom(payload(encoded))

        assertTrue(message.hasPairingOption())
        assertEquals(Pairingmessage.RoleType.ROLE_TYPE_INPUT, message.pairingOption.preferredRole)
        assertEquals(1, message.pairingOption.inputEncodingsCount)

        val encoding = message.pairingOption.getInputEncodings(0)
        assertEquals(Pairingmessage.PairingEncoding.EncodingType.ENCODING_TYPE_HEXADECIMAL, encoding.type)
        assertEquals(6, encoding.symbolLength)
    }

    @Test
    fun `pairing configuration uses input role and hexadecimal encoding`() {
        val encoded = PairingMessageManager().createConfigMessage()
        val message = Pairingmessage.PairingMessage.parseFrom(payload(encoded))

        assertTrue(message.hasPairingConfiguration())
        assertEquals(Pairingmessage.RoleType.ROLE_TYPE_INPUT, message.pairingConfiguration.clientRole)
        assertEquals(
            Pairingmessage.PairingEncoding.EncodingType.ENCODING_TYPE_HEXADECIMAL,
            message.pairingConfiguration.encoding.type
        )
        assertEquals(6, message.pairingConfiguration.encoding.symbolLength)
    }

    @Test
    fun `secret message preserves secret bytes`() {
        val secret = byteArrayOf(0x01, 0x23, 0x45, 0x67)
        val message = PairingMessageManager().createSecretMessageProto(secret)

        assertTrue(message.hasPairingSecret())
        assertArrayEquals(secret, message.pairingSecret.secret.toByteArray())
        assertEquals(Pairingmessage.PairingMessage.Status.STATUS_OK, message.status)
        assertEquals(2, message.protocolVersion)
    }

    @Test
    fun `framed messages declare their payload length`() {
        val command = RemoteMessageManager().createPowerCommand()
        val pairing = PairingMessageManager().createPairingOption()

        assertFrameLength(command)
        assertFrameLength(pairing)
        assertFalse(command.isEmpty())
        assertFalse(pairing.isEmpty())
    }

    private fun payload(framedMessage: ByteArray): ByteArray {
        assertFrameLength(framedMessage)
        return framedMessage.copyOfRange(1, framedMessage.size)
    }

    private fun assertFrameLength(framedMessage: ByteArray) {
        assertTrue("Un message encadré doit contenir au moins le préfixe de longueur", framedMessage.isNotEmpty())
        val declaredLength = framedMessage[0].toInt() and 0xff
        assertEquals(framedMessage.size - 1, declaredLength)
    }
}
