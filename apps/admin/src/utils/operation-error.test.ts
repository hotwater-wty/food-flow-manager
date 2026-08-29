import axios from 'axios'
import { describe, expect, it } from 'vitest'
import { OperationError, toOperationError, unwrapMutation, unwrapQuery } from './operation-error'

describe('operation response contract', () => {
  it('accepts code=1 with null data for mutations', async () => {
    await expect(
      unwrapMutation(Promise.resolve({ data: { code: 1, msg: 'success', data: null } }), '失败'),
    ).resolves.toBeNull()
  })

  it('requires non-null query data', async () => {
    await expect(
      unwrapQuery(Promise.resolve({ data: { code: 1, msg: 'success', data: null } }), '查询失败'),
    ).rejects.toMatchObject({
      detail: { errorCode: 'EMPTY_RESPONSE' },
    })
  })

  it('keeps the backend business error code', async () => {
    await expect(
      unwrapMutation(
        Promise.resolve({
          data: { code: 0, msg: '桌位占用', data: null, errorCode: 'TABLE_IN_USE' },
          config: { method: 'post', url: '/admin/tables/1/disable' },
        }),
        '失败',
      ),
    ).rejects.toMatchObject({
      detail: {
        kind: 'business',
        errorCode: 'TABLE_IN_USE',
        message: '桌位占用',
        request: { method: 'POST', url: '/admin/tables/1/disable' },
      },
    })
  })
})

describe('operation error normalization', () => {
  it('maps timeout and HTTP errors', () => {
    const timeout = new axios.AxiosError('timeout', 'ECONNABORTED', {
      method: 'post',
      url: '/admin/tables/1/disable',
      headers: new axios.AxiosHeaders(),
    })
    expect(toOperationError(timeout).detail).toMatchObject({ kind: 'network', errorCode: 'NETWORK_TIMEOUT' })

    const http = new axios.AxiosError(
      'forbidden',
      'ERR_BAD_RESPONSE',
      { method: 'post', url: '/admin/tables/1/disable', headers: new axios.AxiosHeaders() },
      undefined,
      {
        data: { code: 0, msg: '无权限', data: null },
        status: 403,
        statusText: 'Forbidden',
        headers: {},
        config: { headers: new axios.AxiosHeaders() },
      },
    )
    expect(toOperationError(http).detail).toMatchObject({
      kind: 'http',
      errorCode: 'HTTP_403',
      httpStatus: 403,
      message: '无权限',
    })
  })

  it('does not wrap an OperationError twice', () => {
    const error = new OperationError({ kind: 'business', errorCode: 'KNOWN', message: 'known' })
    expect(toOperationError(error)).toBe(error)
  })
})
