import { sm2 } from 'sm-crypto'

let publicKey = ''

export async function initSM2Key() {
  try {
    const response = await fetch('/api/auth/sm2/public-key')
    const res = await response.json()
    if (res.code === 200 && res.data) {
      publicKey = res.data
    }
  } catch (e) {
    console.warn('SM2公钥获取失败，将使用明文传输')
  }
}

export function sm2Encrypt(plainText) {
  if (!publicKey) {
    return { encrypted: false, data: plainText }
  }
  try {
    const cipherText = sm2.doEncrypt(plainText, publicKey, 0)
    return { encrypted: true, data: cipherText }
  } catch (e) {
    console.warn('SM2加密失败，将使用明文传输')
    return { encrypted: false, data: plainText }
  }
}

export function getPublicKey() {
  return publicKey
}
