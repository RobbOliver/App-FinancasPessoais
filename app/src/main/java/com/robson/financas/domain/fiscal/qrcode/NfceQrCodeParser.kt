package com.robson.financas.domain.fiscal.qrcode

/**
 * O QR Code da NFC-e não carrega os itens — só uma URL de consulta pública do portal do
 * estado com a chave de 44 dígitos embutida (formato varia por estado: querystring `p=`,
 * caminho, hash). A invariante estável entre todos os estados é a própria sequência de 44
 * dígitos, então é isso que extraímos, sem depender do formato exato da URL.
 */
object NfceQrCodeParser {
    private val accessKeyRegex = Regex("""\d{44}""")

    fun extractAccessKey(qrContent: String): String? = accessKeyRegex.find(qrContent)?.value

    fun looksLikeNfceQrCode(qrContent: String): Boolean = extractAccessKey(qrContent) != null
}
