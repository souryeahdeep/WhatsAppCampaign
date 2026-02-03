import { Fragment, useEffect, useState } from 'react'
import './App.css'

const API_BASE = 'http://localhost:8080'
const POLL_INTERVAL_MS = 2000

function formatDate(value) {
  if (!value) return '—'
  const date = new Date(value)
  return date.toLocaleString(undefined, {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function buildStats(messages) {
  const totals = messages.reduce(
    (acc, message) => {
      acc.total += 1
      acc[message.status] = (acc[message.status] || 0) + 1
      if (message.deliveredAt) acc.deliveryRate += 1
      if (message.readAt) acc.readRate += 1
      return acc
    },
    { total: 0, SENT: 0, DELIVERED: 0, READ: 0, FAILED: 0, deliveryRate: 0, readRate: 0 },
  )

  return {
    total: totals.total,
    sent: totals.SENT,
    delivered: totals.DELIVERED,
    read: totals.READ,
    failed: totals.FAILED,
    deliveryRate: totals.total ? Math.round((totals.deliveryRate / totals.total) * 100) : 0,
    readRate: totals.total ? Math.round((totals.readRate / totals.total) * 100) : 0,
  }
}

function StatusPill({ status }) {
  const intent = {
    SENT: 'info',
    DELIVERED: 'success',
    READ: 'accent',
    FAILED: 'danger',
  }[status]

  return <span className={`pill pill-${intent}`}>{status}</span>
}

function App() {
  const [campaign, setCampaign] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [exportJob, setExportJob] = useState(null)
  const [exporting, setExporting] = useState(false)

  useEffect(() => {
    let cancelled = false

    async function load() {
      setLoading(true)
      setError('')
      try {
        const response = await fetch(`${API_BASE}/campaigns/1`)
        if (!response.ok) throw new Error(`Request failed: ${response.status}`)
        const data = await response.json()
        console.log(data);
        
        if (!cancelled) setCampaign(data)
      } catch (err) {
        if (!cancelled) setError('Unable to load campaign data.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    load()
    return () => {
      cancelled = true
    }
  }, [])

  const messages = campaign?.messages || []
  const stats = buildStats(messages)

  async function exportCsv() {
    if (!campaign || exporting) return

    try {
      setError('')
      setExporting(true)
      const response = await fetch(`${API_BASE}/campaigns/${campaign.id}/export`, {
        method: 'POST',
      })
      if (!response.ok) throw new Error('Export failed')

      const payload = await response.json()
      const job = {
        id: payload.id ?? payload.jobId,
        campaignId: payload.campaignId,
        status: payload.status,
      }
      setExportJob(job)
    } catch (err) {
      setError('Unable to start export right now.')
    } finally {
      setExporting(false)
    }
  }

  useEffect(() => {
    if (!exportJob || exportJob.status !== 'PENDING') return

    let cancelled = false
    const timer = setTimeout(async () => {
      try {
        const jobId = exportJob.id
        const response = await fetch(`${API_BASE}/exports/${jobId}/status`)
        if (response.status === 404) {
          if (!cancelled) {
            setError('Export job not found.')
            setExportJob(null)
          }
          return
        }
        if (!response.ok) throw new Error('Status check failed')
        const data = await response.json()
      console.log(data);
      
        if (!cancelled) {
          setExportJob((prev) =>
            prev
              ? {
                  ...prev,
                  id: data.id ?? data.jobId ?? prev.id,
                  status: data.status,
                }
              : prev,
          )
        }
      } catch (err) {
        if (!cancelled) setError('Unable to check export status.')
      }
    }, POLL_INTERVAL_MS)

    return () => {
      cancelled = true
      clearTimeout(timer)
    }
  }, [exportJob])

  async function downloadCsv() {
    if (!exportJob || exportJob.status !== 'COMPLETED') return

    try {
      setError('')
      console.log(exportJob.id);
      
      const response = await fetch(`${API_BASE}/campaigns/exports/${exportJob.id}/download`)
      if (!response.ok) throw new Error('Download failed')

      const disposition = response.headers.get('content-disposition') || ''
      const filenameMatch = disposition.match(/filename="?([^";]+)"?/i)
      const inferredName = filenameMatch?.[1]

      const blob = await response.blob()
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.rel = 'noopener'
      link.style.display = 'none'
      link.download = inferredName || `campaign-${exportJob.campaignId || campaign?.id || exportJob.id}.csv`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(url)
    } catch (err) {
      setError('Unable to download export right now.')
    }
  }

  return (
    <div className="page">
      <header className="page__header">
        <div>
          <p className="eyebrow">Campaign</p>
          <h1>{campaign?.name || 'Loading…'}</h1>
          <div className="meta-row">
            <span>Campaign ID: {campaign?.id || '—'}</span>
            <span>Business ID: {campaign?.businessId || '—'}</span>
            <span>Created: {formatDate(campaign?.createdAt)}</span>
          </div>
        </div>
        <div className="actions">
          <button
            type="button"
            className="ghost"
            onClick={exportJob?.status === 'COMPLETED' ? downloadCsv : exportCsv}
            disabled={!campaign || !messages.length || exporting || exportJob?.status === 'PENDING'}
          >
            {exportJob?.status === 'COMPLETED'
              ? 'Download'
              : exportJob?.status === 'PENDING'
              ? 'Exporting…'
              : exportJob?.status === 'FAILED'
              ? 'Retry Export'
              : exporting
              ? 'Starting…'
              : 'Export CSV'}
          </button>
          <div className="badge">Live</div>
        </div>
      </header>

      {exportJob && (
        <div className="card" style={{ marginTop: '12px' }}>
          <p className="label">Export job</p>
          <p className="caption">Job ID: {exportJob.id} • Status: {exportJob.status}</p>
        </div>
      )}

      <section className="summary">
        <div className="card">
          <p className="label">Total Messages</p>
          <p className="value">{stats.total}</p>
        </div>
        <div className="card">
          <p className="label">Delivered</p>
          <p className="value">{stats.delivered}</p>
          <p className="caption">Delivery rate {stats.deliveryRate}%</p>
        </div>
        <div className="card">
          <p className="label">Read</p>
          <p className="value">{stats.read}</p>
          <p className="caption">Read rate {stats.readRate}%</p>
        </div>
        <div className="card">
          <p className="label">Failed</p>
          <p className="value danger">{stats.failed}</p>
          <p className="caption">Follow up on errors</p>
        </div>
      </section>

      <section className="table">
        <div className="table__header">
          <h2>Messages</h2>
          <p className="caption">Detail per recipient</p>
        </div>
        <div className="table__grid">
          <div className="table__head">ID</div>
          <div className="table__head">Customer</div>
          <div className="table__head">Phone</div>
          <div className="table__head">Status</div>
          <div className="table__head">Sent</div>
          <div className="table__head">Delivered</div>
          <div className="table__head">Read</div>
          <div className="table__head">Error</div>
          {messages.map((message) => (
            <Fragment key={message.id}>
              <div className="cell">#{message.id}</div>
              <div className="cell">Customer {message.customerId}</div>
              <div className="cell">{message.phoneNumber}</div>
              <div className="cell">
                <StatusPill status={message.status} />
              </div>
              <div className="cell">{formatDate(message.sentAt)}</div>
              <div className="cell">{formatDate(message.deliveredAt)}</div>
              <div className="cell">{formatDate(message.readAt)}</div>
              <div className="cell">{message.errorReason || '—'}</div>
            </Fragment>
          ))}
          {!messages.length && !loading && (
            <div className="cell muted" style={{ gridColumn: '1 / -1' }}>
              No messages yet.
            </div>
          )}
        </div>
      </section>

      {loading && (
        <div className="card" style={{ marginTop: '12px' }}>
          <p className="label">Loading campaign…</p>
        </div>
      )}

      {error && (
        <div className="card error" style={{ marginTop: '12px' }}>
          <p className="label">{error}</p>
        </div>
      )}
    </div>
  )
}

export default App
