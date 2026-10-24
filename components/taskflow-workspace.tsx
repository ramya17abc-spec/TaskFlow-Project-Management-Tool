'use client'

import { useMemo, useState } from 'react'
import useSWR from 'swr'
import {
  Activity, ArrowDown, ArrowUp, Bell, CalendarDays, Check, CheckCheck, ChevronDown,
  ChevronRight, CircleHelp, Clock3, Command, Filter, FolderKanban,
  LayoutDashboard, ListTodo, Menu, MessageCircle, MoreHorizontal, Plus,
  Search, Settings2, SlidersHorizontal, Sparkles, Users, X,
} from 'lucide-react'

type Status = 'Backlog' | 'In progress' | 'In review' | 'Done'
type Priority = 'Low' | 'Medium' | 'High'
type Task = { id: number; title: string; project: string; status: Status; priority: Priority; due: string; assignee: string; initials: string; color: string; comments: number; tag: string }
type RemoteProject = { id: number; name: string; description?: string; taskCount: number; completedCount: number }
type RemoteMember = { id: number; displayName: string; email: string; role: string }
type RemoteTask = { id: number; title: string; projectId: number; projectName: string; status: string; priority: string; dueDate?: string; assigneeName?: string; assigneeId?: number; commentCount: number; tag?: string }
type Session = { token: string; user: { id: number; displayName: string; email: string } }
type RemoteComment = { id: number; body: string; authorId: number; authorName: string; createdAt: string }
const API_BASE = process.env.NEXT_PUBLIC_TASKFLOW_API_URL ?? 'http://localhost:8080'

async function apiRequest<T>(path: string, token: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, { ...options, headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}`, ...options.headers } })
  if (!response.ok) {
    const payload = await response.json().catch(() => null)
    throw new Error(payload?.message ?? `Request failed (${response.status})`)
  }
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}

function apiStatus(status: Status) {
  return status.toUpperCase().replaceAll(' ', '_')
}

function displayStatus(status: string): Status {
  return status === 'IN_PROGRESS' ? 'In progress' : status === 'IN_REVIEW' ? 'In review' : status === 'DONE' ? 'Done' : 'Backlog'
}

function displayPriority(priority: string): Priority {
  return priority === 'HIGH' ? 'High' : priority === 'LOW' ? 'Low' : 'Medium'
}

function initials(name = 'Team member') { return name.split(' ').map((part) => part[0]).join('').slice(0, 2).toUpperCase() }

const initialTasks: Task[] = [
  { id: 1, title: 'Map the onboarding journey', project: 'Website redesign', status: 'Backlog', priority: 'High', due: 'Oct 24', assignee: 'Olivia Rhye', initials: 'OR', color: 'rose', comments: 3, tag: 'Research' },
  { id: 2, title: 'Write product positioning', project: 'Website redesign', status: 'Backlog', priority: 'Medium', due: 'Oct 26', assignee: 'Phoenix Baker', initials: 'PB', color: 'blue', comments: 1, tag: 'Content' },
  { id: 3, title: 'Audit competitor landing pages', project: 'Website redesign', status: 'Backlog', priority: 'Low', due: 'Oct 28', assignee: 'Lana Steiner', initials: 'LS', color: 'amber', comments: 0, tag: 'Research' },
  { id: 4, title: 'Build responsive navigation', project: 'Website redesign', status: 'In progress', priority: 'High', due: 'Oct 23', assignee: 'Demi Wilkinson', initials: 'DW', color: 'violet', comments: 5, tag: 'Development' },
  { id: 5, title: 'Create design system foundations', project: 'Website redesign', status: 'In progress', priority: 'High', due: 'Oct 25', assignee: 'Olivia Rhye', initials: 'OR', color: 'rose', comments: 2, tag: 'Design' },
  { id: 6, title: 'Design mobile checkout flow', project: 'Mobile app', status: 'In progress', priority: 'Medium', due: 'Oct 27', assignee: 'Candice Wu', initials: 'CW', color: 'green', comments: 4, tag: 'Design' },
  { id: 7, title: 'Review homepage prototypes', project: 'Website redesign', status: 'In review', priority: 'Medium', due: 'Oct 23', assignee: 'Phoenix Baker', initials: 'PB', color: 'blue', comments: 8, tag: 'Design' },
  { id: 8, title: 'Set up analytics events', project: 'Website redesign', status: 'In review', priority: 'Low', due: 'Oct 29', assignee: 'Demi Wilkinson', initials: 'DW', color: 'violet', comments: 2, tag: 'Development' },
  { id: 9, title: 'Finalize brand guidelines', project: 'Brand refresh', status: 'Done', priority: 'High', due: 'Oct 21', assignee: 'Lana Steiner', initials: 'LS', color: 'amber', comments: 6, tag: 'Brand' },
  { id: 10, title: 'Publish the design library', project: 'Website redesign', status: 'Done', priority: 'Medium', due: 'Oct 20', assignee: 'Candice Wu', initials: 'CW', color: 'green', comments: 3, tag: 'Design' },
]

const statuses: Status[] = ['Backlog', 'In progress', 'In review', 'Done']
const members = [
  { name: 'Olivia Rhye', initials: 'OR', color: 'rose' }, { name: 'Phoenix Baker', initials: 'PB', color: 'blue' },
  { name: 'Lana Steiner', initials: 'LS', color: 'amber' }, { name: 'Demi Wilkinson', initials: 'DW', color: 'violet' },
  { name: 'Candice Wu', initials: 'CW', color: 'green' },
]
const workspaceMembers = [
  { id: 1, displayName: 'Jordan Davis', email: 'demo@taskflow.local', role: 'ADMIN' },
  { id: 2, displayName: 'Olivia Rhye', email: 'olivia@taskflow.local', role: 'MEMBER' },
  { id: 3, displayName: 'Phoenix Baker', email: 'phoenix@taskflow.local', role: 'MEMBER' },
  { id: 4, displayName: 'Lana Steiner', email: 'lana@taskflow.local', role: 'MEMBER' },
]

function Avatar({ initials, color, small = false }: { initials: string; color: string; small?: boolean }) {
  return <span role="img" aria-label={initials} className={`avatar avatar-${color} ${small ? 'avatar-small' : ''}`}>{initials}</span>
}

export default function TaskFlowWorkspace() {
  const [tasks, setTasks] = useState(initialTasks)
  const [session, setSession] = useState<Session | null>(null)
  const [query, setQuery] = useState('')
  const [priorityFilter, setPriorityFilter] = useState('All priorities')
  const [activeNav, setActiveNav] = useState('Board')
  const [activeProject, setActiveProject] = useState('Website redesign')
  const [modal, setModal] = useState<'task' | 'login' | 'signup' | 'project' | 'invite' | 'profile' | null>(null)
  const [selectedTask, setSelectedTask] = useState<Task | null>(null)
  const [notificationsOpen, setNotificationsOpen] = useState(false)
  const [mobileNavOpen, setMobileNavOpen] = useState(false)
  const [newTitle, setNewTitle] = useState('')
  const [newPriority, setNewPriority] = useState<Priority>('Medium')
  const [newTaskProjectId, setNewTaskProjectId] = useState('')
  const [newDueDate, setNewDueDate] = useState('')
  const [newAssignee, setNewAssignee] = useState('')
  const [newProjectName, setNewProjectName] = useState('')
  const [newProjectDescription, setNewProjectDescription] = useState('')
  const [localProjectNames, setLocalProjectNames] = useState(['Website redesign', 'Mobile app', 'Brand refresh'])
  const [inviteEmail, setInviteEmail] = useState('')
  const [profileName, setProfileName] = useState('')
  const [authEmail, setAuthEmail] = useState('')
  const [authPassword, setAuthPassword] = useState('')
  const [authName, setAuthName] = useState('')
  const [authError, setAuthError] = useState('')
  const [authPending, setAuthPending] = useState(false)
  const [newComment, setNewComment] = useState('')
  const [toast, setToast] = useState('')

  const { data: remoteProjects, mutate: refreshProjects } = useSWR<RemoteProject[]>(session ? ([`${API_BASE}/api/projects`, session.token] as const) : null, ([url, token]: readonly [string, string]) => apiRequest<RemoteProject[]>(url.replace(API_BASE, ''), token))
  const selectedProject = remoteProjects?.find((project) => project.name === activeProject) ?? (activeProject === 'All projects' ? undefined : remoteProjects?.[0])
  const taskEndpoint = activeProject === 'All projects' ? '/api/tasks' : selectedProject ? `/api/projects/${selectedProject.id}/tasks` : null
  const { data: remoteTasks, mutate: refreshTasks } = useSWR<RemoteTask[]>(session && taskEndpoint ? ([`${API_BASE}${taskEndpoint}`, session.token] as const) : null, ([url, token]: readonly [string, string]) => apiRequest<RemoteTask[]>(url.replace(API_BASE, ''), token))
  const { data: remoteMembers, mutate: refreshMembers } = useSWR<RemoteMember[]>(session && selectedProject ? ([`${API_BASE}/api/projects/${selectedProject.id}/members`, session.token] as const) : null, ([url, token]: readonly [string, string]) => apiRequest<RemoteMember[]>(url.replace(API_BASE, ''), token))
  const { data: remoteComments, mutate: refreshComments } = useSWR<RemoteComment[]>(session && selectedTask ? ([`${API_BASE}/api/tasks/${selectedTask.id}/comments`, session.token] as const) : null, ([url, token]: readonly [string, string]) => apiRequest<RemoteComment[]>(url.replace(API_BASE, ''), token))
  const { data: remoteNotifications} = useSWR<{ id: number; message: string; read: boolean; createdAt: string }[]>(session ? ([`${API_BASE}/api/notifications`, session.token] as const) : null, ([url, token]: readonly [string, string]) => apiRequest(url.replace(API_BASE, ''), token))
  const boardTasks = session ? (remoteTasks ?? []).map((task) => ({ id: task.id, title: task.title, project: task.projectName, status: displayStatus(task.status), priority: displayPriority(task.priority), due: task.dueDate ? new Date(`${task.dueDate}T12:00:00`).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }) : 'No date', assignee: task.assigneeName ?? 'Unassigned', initials: initials(task.assigneeName), color: 'indigo', comments: task.commentCount, tag: task.tag ?? 'Task' })) : tasks

  const visibleTasks = useMemo(() => boardTasks.filter((task) =>
    (activeProject === 'All projects' || task.project === activeProject) &&
    (activeNav !== 'My tasks' || task.assignee === (session?.user.displayName ?? 'Jordan Davis')) &&
    (priorityFilter === 'All priorities' || task.priority === priorityFilter) &&
    `${task.title} ${task.assignee} ${task.tag}`.toLowerCase().includes(query.toLowerCase())
  ), [boardTasks, activeProject, activeNav, session?.user.displayName, priorityFilter, query])
  const totalCount = session ? (remoteTasks?.length ?? 0) : 24
  const completedCount = session ? (remoteTasks?.filter((task) => task.status === 'DONE').length ?? 0) : 16
  const completionPercent = totalCount ? Math.round(completedCount / totalCount * 100) : 0

  function notify(message: string) {
    setToast(message)
    window.setTimeout(() => setToast(''), 2600)
  }

  async function moveTask(taskId: number, status: Status) {
    if (session && selectedProject && remoteTasks) {
      try {
        await apiRequest(`/api/tasks/${taskId}/status`, session.token, { method: 'PATCH', body: JSON.stringify({ status: apiStatus(status) }) })
        await refreshTasks()
      } catch (error) { notify(error instanceof Error ? error.message : 'Could not update task') ; return }
    } else {
      setTasks((current) => current.map((task) => task.id === taskId ? { ...task, status } : task))
    }
    notify(`Task moved to ${status}`)
  }

  async function addTask(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!newTitle.trim()) return
    const taskProject = activeProject === 'All projects'
      ? session
        ? remoteProjects?.find((project) => project.id === Number(newTaskProjectId)) ?? remoteProjects?.[0]
        : { id: Number(newTaskProjectId) || 0, name: localProjectNames[Number(newTaskProjectId) || 0] }
      : session ? selectedProject : { id: 0, name: activeProject }
    if (!taskProject) { notify('Choose a project before adding tasks'); return }
    if (session) {
      try {
        await apiRequest(`/api/projects/${taskProject.id}/tasks`, session.token, { method: 'POST', body: JSON.stringify({ title: newTitle.trim(), status: 'BACKLOG', priority: newPriority.toUpperCase(), dueDate: newDueDate || null, tag: 'Task', assigneeId: Number(newAssignee) || session.user.id }) })
        await refreshTasks()
      } catch (error) { notify(error instanceof Error ? error.message : 'Could not create task'); return }
    } else {
      const due = newDueDate ? new Date(`${newDueDate}T12:00:00`).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }) : 'No date'
      const task: Task = { id: Date.now(), title: newTitle.trim(), project: taskProject.name, status: 'Backlog', priority: newPriority, due, assignee: newAssignee || 'You', initials: initials(newAssignee || 'You'), color: 'indigo', comments: 0, tag: 'Task' }
      setTasks((current) => [task, ...current])
    }
    setNewTitle('')
    setNewDueDate('')
    setModal(null)
    notify('Task added to your board')
  }

  async function authenticate(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setAuthError('')
    setAuthPending(true)
    try {
      const response = await fetch(`${API_BASE}/api/auth/${modal === 'signup' ? 'signup' : 'login'}`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(modal === 'signup' ? { displayName: authName, email: authEmail, password: authPassword } : { email: authEmail, password: authPassword }) })
      const payload = await response.json()
      if (!response.ok) throw new Error(payload?.message ?? 'Unable to sign in')
      setSession({ token: payload.token, user: payload.user })
      setModal(null)
      setAuthPassword('')
      notify(`Welcome, ${payload.user.displayName}`)
      void refreshProjects()
    } catch (error) {
      setAuthError(error instanceof Error ? error.message : 'The API could not be reached. Start the backend or continue in demo mode.')
    } finally { setAuthPending(false) }
  }

  async function addComment(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!selectedTask || !newComment.trim()) return
    if (session) {
      try {
        await apiRequest(`/api/tasks/${selectedTask.id}/comments`, session.token, { method: 'POST', body: JSON.stringify({ body: newComment.trim() }) })
        await refreshComments()
        await refreshTasks()
      } catch (error) { notify(error instanceof Error ? error.message : 'Could not add comment'); return }
    } else {
      setSelectedTask({ ...selectedTask, comments: selectedTask.comments + 1 })
      setTasks((current) => current.map((task) => task.id === selectedTask.id ? { ...task, comments: task.comments + 1 } : task))
    }
    setNewComment('')
    notify('Comment added')
  }

  async function createProject(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!newProjectName.trim()) return
    if (session) {
      try {
        await apiRequest('/api/projects', session.token, { method: 'POST', body: JSON.stringify({ name: newProjectName.trim(), description: newProjectDescription, color: 'violet' }) })
        await refreshProjects()
      } catch (error) { notify(error instanceof Error ? error.message : 'Could not create project'); return }
    } else setLocalProjectNames((current) => [...current, newProjectName.trim()])
    setActiveProject(newProjectName.trim())
    setNewProjectName('')
    setNewProjectDescription('')
    setModal(null)
    notify('Project created')
  }

  async function inviteMember(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!inviteEmail.trim()) return
    if (session && selectedProject) {
      try {
        await apiRequest(`/api/projects/${selectedProject.id}/members`, session.token, { method: 'POST', body: JSON.stringify({ email: inviteEmail.trim(), role: 'MEMBER' }) })
        await refreshMembers()
      } catch (error) { notify(error instanceof Error ? error.message : 'Could not add team member'); return }
    }
    setInviteEmail('')
    setModal(null)
    notify(session ? 'Team member added to project' : 'Invite link copied to clipboard')
  }

  async function saveProfile(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (session) {
      try {
        const updated = await apiRequest<Session['user']>('/api/users/me', session.token, { method: 'PATCH', body: JSON.stringify({ displayName: profileName, jobTitle: 'Team member' }) })
        setSession({ ...session, user: updated })
      } catch (error) { notify(error instanceof Error ? error.message : 'Could not update profile'); return }
    }
    setModal(null)
    notify('Profile updated')
  }

  return (
    <main className="app-shell">
      <aside className={`sidebar ${mobileNavOpen ? 'sidebar-open' : ''}`}>
        <div className="brand-row"><span className="brand-mark"><CheckCheck size={20} strokeWidth={2.5} /></span><span className="brand-name">taskflow<span className="brand-dot">.</span></span><button className="icon-button mobile-close" aria-label="Close navigation" onClick={() => setMobileNavOpen(false)}><X size={18} /></button></div>
        <button className="workspace-picker"><span className="workspace-icon">S</span><span className="workspace-copy"><strong>Studio North</strong><small>Free workspace</small></span><ChevronDown size={16} /></button>
        <div className="sidebar-section-label">WORKSPACE</div>
        <nav className="nav-list" aria-label="Main navigation">
          {[{ label: 'Overview', icon: LayoutDashboard }, { label: 'My tasks', icon: ListTodo }, { label: 'Board', icon: FolderKanban }, { label: 'Activity', icon: Activity }].map(({ label, icon: Icon }) => <button key={label} className={`nav-item ${activeNav === label ? 'nav-active' : ''}`} onClick={() => { setActiveNav(label); setMobileNavOpen(false) }}><Icon size={17} /><span>{label}</span>{label === 'My tasks' && <span className="nav-count">4</span>}</button>)}
        </nav>
        <div className="sidebar-section-heading"><span className="sidebar-section-label">YOUR PROJECTS</span><button aria-label="Create project" className="tiny-icon" onClick={() => setModal('project')}><Plus size={15} /></button></div>
        <div className="project-list">
          {(session ? (remoteProjects ?? []) : localProjectNames.map((name, index) => ({ id: index, name, taskCount: tasks.filter((task) => task.project === name).length }))).map((project, index) => <button key={project.id} className={`project-link ${activeProject === project.name ? 'project-selected' : ''}`} onClick={() => { setActiveProject(project.name); setActiveNav('Board') }}><span className={`project-color ${['color-purple', 'color-orange', 'color-teal'][index % 3]}`} />{project.name}<span className="project-number">{project.taskCount}</span></button>)}
          <button className="project-link muted-project" onClick={() => { setActiveProject('All projects'); setActiveNav('Board') }}><span className="project-color color-gray" />All projects</button>
        </div>
        <div className="sidebar-spacer" />
        <div className="sidebar-bottom">
          <button className={`nav-item ${activeNav === 'Team' ? 'nav-active' : ''}`} onClick={() => setActiveNav('Team')}><Users size={17} /><span>Team members</span><span className="nav-count">8</span></button>
          <button className={`nav-item ${activeNav === 'Settings' ? 'nav-active' : ''}`} onClick={() => setActiveNav('Settings')}><Settings2 size={17} /><span>Settings</span></button>
          <div className="upgrade-card"><span className="upgrade-icon"><Sparkles size={15} /></span><strong>Make room for more</strong><p>Upgrade your workspace to unlock unlimited projects.</p><button onClick={() => notify('You are already on the latest plan')}>Explore plans <ArrowUp size={13} /></button></div>
          <div className="profile-row"><Avatar initials={initials(session?.user.displayName ?? 'Jordan Davis')} color="indigo" /><div className="profile-details"><strong>{session?.user.displayName ?? 'Jordan Davis'}</strong><small>{session?.user.email ?? 'jordan@studionorth.co'}</small></div><button className="tiny-icon" aria-label="Edit profile" onClick={() => { setProfileName(session?.user.displayName ?? 'Jordan Davis'); setModal(session ? 'profile' : 'login') }}><MoreHorizontal size={18} /></button></div>
        </div>
      </aside>

      <section className="main-area">
        <header className="topbar">
          <div className="topbar-left"><button className="icon-button menu-button" aria-label="Open navigation" onClick={() => setMobileNavOpen(true)}><Menu size={20} /></button><div className="breadcrumb"><span>Projects</span><ChevronRight size={14} /><strong>{activeProject === 'All projects' ? 'All projects' : activeProject}</strong></div></div>
          <div className="topbar-actions"><label className="search-field"><Search size={16} /><input aria-label="Search tasks" placeholder="Search anything..." value={query} onChange={(event) => setQuery(event.target.value)} /><kbd><Command size={11} /> K</kbd></label><span className={`api-state ${session ? 'api-connected' : ''}`}><i />{session ? 'API connected' : 'Demo workspace'}</span><button className="icon-button notification-button" aria-label="Notifications" onClick={() => setNotificationsOpen(!notificationsOpen)}><Bell size={18} /><i /></button>{session ? <button className="session-avatar" title="Sign out" aria-label="Sign out" onClick={() => { setSession(null); notify('Signed out') }}><Avatar initials={initials(session.user.displayName)} color="indigo" small /></button> : <button className="signin-link" onClick={() => { setAuthError(''); setModal('login') }}>Sign in</button>}</div>
          {notificationsOpen && <div className="notification-popover"><div className="popover-heading"><strong>Notifications</strong><button onClick={() => setNotificationsOpen(false)} aria-label="Close notifications"><X size={15} /></button></div>{session ? (remoteNotifications ?? []).slice(0, 4).map((notification) => <div className="notification-item" key={notification.id}><span className={`notification-dot ${notification.read ? 'dot-purple' : 'dot-blue'}`} /><p>{notification.message}<small>{new Date(notification.createdAt).toLocaleString()}</small></p></div>) : <><div className="notification-item"><span className="notification-dot dot-blue" /><p><strong>Phoenix</strong> mentioned you in <b>Homepage prototypes</b><small>12 minutes ago</small></p></div><div className="notification-item"><span className="notification-dot dot-purple" /><p><strong>Olivia</strong> completed <b>Brand guidelines</b><small>1 hour ago</small></p></div></>}<button className="notification-footer" onClick={() => { setNotificationsOpen(false); setActiveNav('Activity') }}>View all activity <ChevronRight size={14} /></button></div>}
        </header>

        <div className="page-content">
          <div className="page-heading"><div><div className="eyebrow"><span className="eyebrow-dot" />TEAM WORKSPACE <span className="eyebrow-separator">/</span> OCTOBER 2026</div><h1>{activeNav === 'Board' ? 'Project board' : activeNav}</h1><p className="page-description">A clear view of what&apos;s moving and what&apos;s next.</p></div><div className="heading-actions"><div className="avatar-stack" aria-label={`${session ? remoteMembers?.length ?? 0 : workspaceMembers.length} project members`}>{(session ? (remoteMembers ?? []).slice(0, 4).map((member) => ({ initials: initials(member.displayName), color: 'indigo' })) : workspaceMembers.slice(0, 4).map((member, index) => ({ initials: initials(member.displayName), color: ['indigo', 'rose', 'blue', 'amber'][index] }))).map((member, index) => <Avatar key={`${member.initials}-${index}`} initials={member.initials} color={member.color} small />)}{(session ? remoteMembers?.length ?? 0 : workspaceMembers.length) > 4 && <span className="avatar-more">+{(session ? remoteMembers?.length ?? 0 : workspaceMembers.length) - 4}</span>}</div><button className="button-secondary invite-button" onClick={() => selectedProject ? setModal('invite') : notify('Select a project to invite teammates')}><Users size={15} /> Invite</button><button className="button-primary" onClick={() => setModal('task')}><Plus size={16} /> New task</button></div></div>

          <section className="summary-grid" aria-label="Project summary">
            <article className="summary-card"><div className="summary-top"><span className="summary-label">Total tasks</span><span className="summary-icon icon-violet"><ListTodo size={17} /></span></div><div className="summary-value">{session ? totalCount : 24} <span className="summary-delta"><ArrowUp size={12} /> 12%</span></div><div className="summary-foot">{session ? 'Tasks in this project' : 'vs. last month'}</div><div className="mini-chart bars-purple" aria-hidden="true">{[35, 54, 40, 70, 50, 66, 46, 83, 61, 91, 72, 100].map((h, i) => <i key={i} style={{ height: `${h}%` }} />)}</div></article>
            <article className="summary-card"><div className="summary-top"><span className="summary-label">Completed</span><span className="summary-icon icon-green"><Check size={17} /></span></div><div className="summary-value">{session ? completedCount : 16} <span className="summary-of">/ {session ? totalCount : 24}</span></div><div className="summary-progress"><span style={{ width: `${session ? completionPercent : 67}%` }} /></div><div className="summary-foot">{session ? `${completionPercent}% of project complete` : '67% of project complete'}</div></article>
            <article className="summary-card"><div className="summary-top"><span className="summary-label">Due this week</span><span className="summary-icon icon-amber"><CalendarDays size={17} /></span></div><div className="summary-value">6 <span className="summary-delta delta-neutral"><Clock3 size={12} /> 2 overdue</span></div><div className="summary-foot">Keep the momentum going</div><div className="week-dots"><i /><i /><i /><i /><i /><i /><i /></div></article>
            <article className="summary-card team-summary"><div className="summary-top"><span className="summary-label">Team activity</span><span className="summary-icon icon-blue"><Activity size={17} /></span></div><div className="summary-value">+18% <span className="summary-delta"><ArrowUp size={12} /> this week</span></div><div className="summary-foot">Tasks completed per week</div><div className="activity-line"><svg viewBox="0 0 180 32" role="img" aria-label="Team activity is trending upward"><path d="M2 26 C18 22, 20 29, 34 20 S54 25, 65 15 S82 19, 95 12 S112 17, 126 8 S144 15, 157 4 S170 8, 178 2" /></svg></div></article>
          </section>

          <div className={`board-toolbar ${activeNav === 'Team' || activeNav === 'Activity' ? 'view-hidden' : ''}`}><div className="board-tabs"><button className="tab-active"><LayoutDashboard size={15} /> Board <span>24</span></button><button onClick={() => setActiveNav('List')}><ListTodo size={15} /> List</button><button onClick={() => setActiveNav('Timeline')}><CalendarDays size={15} /> Timeline</button></div><div className="board-controls"><label className="filter-select"><Filter size={14} /><select aria-label="Filter by priority" value={priorityFilter} onChange={(event) => setPriorityFilter(event.target.value)}><option>All priorities</option><option>High</option><option>Medium</option><option>Low</option></select><ChevronDown size={13} /></label><button className="icon-button board-settings" aria-label="Board settings" onClick={() => notify('Board settings saved')}><SlidersHorizontal size={16} /></button></div></div>

          {activeNav === 'Team' ? <section className="team-panel"><div className="team-panel-heading"><div><h2>Project team</h2><p>People collaborating on {activeProject}.</p></div><button className="button-primary" onClick={() => setModal('invite')}><Users size={14} /> Add teammate</button></div><div className="team-grid">{(session ? (remoteMembers ?? []) : workspaceMembers).map((member, index) => <article className="team-card" key={member.id}><Avatar initials={initials(member.displayName)} color={members[index % members.length].color} /><div><strong>{member.displayName}</strong><small>{member.email}</small></div><span className="team-role">{member.role === 'ADMIN' ? 'Admin' : 'Member'}</span></article>)}</div></section> : activeNav === 'Activity' ? <section className="team-panel"><div className="team-panel-heading"><div><h2>Recent activity</h2><p>Updates across your workspace.</p></div></div><div className="activity-feed">{session ? (remoteNotifications ?? []).map((notification) => <article key={notification.id}><span className={`notification-dot ${notification.read ? 'dot-purple' : 'dot-blue'}`} /><p>{notification.message}<small>{new Date(notification.createdAt).toLocaleString()}</small></p></article>) : <><article><span className="notification-dot dot-blue" /><p><strong>Phoenix Baker</strong> mentioned you in <b>Homepage prototypes</b><small>12 minutes ago</small></p></article><article><span className="notification-dot dot-purple" /><p><strong>Olivia Rhye</strong> completed <b>Brand guidelines</b><small>1 hour ago</small></p></article></>}</div></section> : <section className="kanban-board" aria-label="Kanban board">
            {statuses.map((status) => {
              const columnTasks = visibleTasks.filter((task) => task.status === status)
              return <div className="kanban-column" key={status} onDragOver={(event) => event.preventDefault()} onDrop={(event) => { event.preventDefault(); const id = Number(event.dataTransfer.getData('text/plain')); if (id) moveTask(id, status) }}>
                <div className="column-heading"><div className="column-title"><span className={`status-indicator status-${status.toLowerCase().replace(' ', '-')}`} />{status}<span className="column-count">{columnTasks.length}</span></div><button className="tiny-icon" aria-label={`Add task to ${status}`} onClick={() => setModal('task')}><Plus size={16} /></button></div>
                <div className="task-list">{columnTasks.map((task) => <article className="task-card" key={task.id} draggable onDragStart={(event) => event.dataTransfer.setData('text/plain', String(task.id))} onClick={() => setSelectedTask(task)}>
                  <div className="task-card-top"><span className={`task-tag tag-${task.tag.toLowerCase()}`}>{task.tag}</span><button className="tiny-icon task-menu" aria-label={`More options for ${task.title}`} onClick={(event) => { event.stopPropagation(); setSelectedTask(task) }}><MoreHorizontal size={17} /></button></div>
                  <h3>{task.title}</h3><div className="task-meta"><span className={`priority priority-${task.priority.toLowerCase()}`}>{task.priority === 'High' ? <ArrowUp size={12} /> : task.priority === 'Low' ? <ArrowDown size={12} /> : <span className="priority-dash" />}{task.priority}</span><span className={`due-date ${task.due === 'Oct 23' ? 'due-soon' : ''}`}><CalendarDays size={12} />{task.due}</span></div>
                  <div className="task-card-footer"><Avatar initials={task.initials} color={task.color} small /><span className="task-assignee">{task.assignee}</span><span className="task-comments"><MessageCircle size={13} />{task.comments}</span></div>
                </article>)}</div>
                <button className="add-task-inline" onClick={() => setModal('task')}><Plus size={15} /> Add task</button>
              </div>
            })}
          </section>}

          <section className="bottom-insight"><div className="insight-icon"><Sparkles size={16} /></div><p><strong>Nice progress, team.</strong> You&apos;re completing tasks 18% faster than last week. Keep your focus on the 6 items due soon.</p><button onClick={() => setPriorityFilter('High')}>See high-priority tasks <ChevronRight size={14} /></button></section>
          <footer className="page-footer"><span>© 2026 TaskFlow, Inc.</span><span><button><CircleHelp size={13} /> Help center</button><i /> <button>Keyboard shortcuts</button></span></footer>
        </div>
      </section>

      {mobileNavOpen && <button className="sidebar-backdrop" aria-label="Close navigation" onClick={() => setMobileNavOpen(false)} />}
      {modal === 'task' && <div className="modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) setModal(null) }}><section className="modal-card" role="dialog" aria-modal="true" aria-labelledby="new-task-title"><div className="modal-heading"><div><span className="modal-kicker">{activeProject}</span><h2 id="new-task-title">Create a task</h2></div><button className="icon-button" aria-label="Close" onClick={() => setModal(null)}><X size={18} /></button></div><form onSubmit={addTask}><label className="form-label" htmlFor="task-title">Task name</label><input id="task-title" className="form-control" autoFocus placeholder="What needs to get done?" value={newTitle} onChange={(event) => setNewTitle(event.target.value)} required maxLength={140} />{activeProject === 'All projects' && <label><span className="form-label">Project</span><select className="form-control" value={newTaskProjectId || (session ? String(remoteProjects?.[0]?.id ?? '') : '0')} onChange={(event) => { setNewTaskProjectId(event.target.value); setNewAssignee('') }} required><option value="" disabled>Select a project</option>{session ? (remoteProjects ?? []).map((project) => <option key={project.id} value={project.id}>{project.name}</option>) : localProjectNames.map((name, index) => <option key={name} value={index}>{name}</option>)}</select></label>}<div className="form-grid"><label><span className="form-label">Priority</span><select className="form-control" value={newPriority} onChange={(event) => setNewPriority(event.target.value as Priority)}><option>Low</option><option>Medium</option><option>High</option></select></label><label><span className="form-label">Deadline</span><input className="form-control" type="date" value={newDueDate} onChange={(event) => setNewDueDate(event.target.value)} /></label></div><label><span className="form-label">Assignee</span><select className="form-control" value={newAssignee || String(session?.user.id ?? 'You')} onChange={(event) => setNewAssignee(event.target.value)}><option value={session?.user.id ?? 'You'}>{session?.user.displayName ?? 'You'}</option>{remoteMembers ? remoteMembers.filter((member) => member.id !== session?.user.id).map((member) => <option key={member.id} value={member.id}>{member.displayName}</option>) : members.map((member) => <option key={member.name} value={member.name}>{member.name}</option>)}</select></label><div className="modal-actions"><button type="button" className="button-secondary" onClick={() => setModal(null)}>Cancel</button><button type="submit" className="button-primary"><Plus size={15} /> Create task</button></div></form></section></div>}
      {selectedTask && <div className="modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) setSelectedTask(null) }}><section className="modal-card task-detail" role="dialog" aria-modal="true" aria-labelledby="detail-title"><div className="modal-heading"><span className={`task-tag tag-${selectedTask.tag.toLowerCase()}`}>{selectedTask.tag}</span><button className="icon-button" aria-label="Close task details" onClick={() => setSelectedTask(null)}><X size={18} /></button></div><h2 id="detail-title" className="detail-title">{selectedTask.title}</h2><p className="detail-description">Add a little more context for your team. Keep all the moving pieces in one place and make the next step clear.</p><div className="detail-fields"><div><span>Status</span><select value={selectedTask.status} onChange={(event) => { const status = event.target.value as Status; moveTask(selectedTask.id, status); setSelectedTask({ ...selectedTask, status }) }}>{statuses.map((status) => <option key={status}>{status}</option>)}</select></div><div><span>Priority</span><strong className={`priority priority-${selectedTask.priority.toLowerCase()}`}>{selectedTask.priority}</strong></div><div><span>Assignee</span><strong className="detail-assignee"><Avatar initials={selectedTask.initials} color={selectedTask.color} small />{selectedTask.assignee}</strong></div><div><span>Due date</span><strong><CalendarDays size={14} /> {selectedTask.due}, 2026</strong></div></div><div className="comment-heading"><strong>Comments</strong><span>{remoteComments ? remoteComments.length : selectedTask.comments}</span></div>{remoteComments ? remoteComments.map((comment) => <div className="comment-row" key={comment.id}><Avatar initials={initials(comment.authorName)} color="blue" small /><p><strong>{comment.authorName}</strong><span>{comment.body}</span><small>{new Date(comment.createdAt).toLocaleString()}</small></p></div>) : <div className="comment-row"><Avatar initials="PB" color="blue" small /><p><strong>Phoenix Baker</strong><span>Looks good. I&apos;ll share an update with the team after the next review.</span><small>Today at 10:42 AM</small></p></div>}<form className="comment-form" onSubmit={addComment}><Avatar initials={initials(session?.user.displayName ?? 'Jordan Davis')} color="indigo" small /><input aria-label="Write a comment" placeholder="Write a comment..." value={newComment} onChange={(event) => setNewComment(event.target.value)} required maxLength={2000} /><button className="button-primary" type="submit">Send</button></form></section></div>}
      {(modal === 'login' || modal === 'signup') && <div className="modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) setModal(null) }}><section className="modal-card" role="dialog" aria-modal="true" aria-labelledby="auth-title"><div className="modal-heading"><div><span className="modal-kicker">TASKFLOW WORKSPACE</span><h2 id="auth-title">{modal === 'login' ? 'Welcome back' : 'Create your account'}</h2></div><button className="icon-button" aria-label="Close" onClick={() => setModal(null)}><X size={18} /></button></div><form onSubmit={authenticate}>{modal === 'signup' && <label><span className="form-label">Your name</span><input className="form-control" autoComplete="name" value={authName} onChange={(event) => setAuthName(event.target.value)} required maxLength={90} /> </label>}<label><span className="form-label">Email address</span><input className="form-control" type="email" autoComplete="email" value={authEmail} onChange={(event) => setAuthEmail(event.target.value)} required /></label><label><span className="form-label">Password</span><input className="form-control" type="password" autoComplete={modal === 'signup' ? 'new-password' : 'current-password'} value={authPassword} onChange={(event) => setAuthPassword(event.target.value)} required minLength={modal === 'signup' ? 10 : 1} /></label>{authError && <p className="form-error" role="alert">{authError}</p>}<p className="detail-description">Demo account: demo@taskflow.local / TaskFlow123!</p><div className="modal-actions"><button type="button" className="button-secondary" onClick={() => setModal(null)}>Continue in demo</button><button type="submit" className="button-primary" disabled={authPending}>{authPending ? 'Connecting…' : modal === 'login' ? 'Sign in' : 'Create account'}</button></div></form><button className="auth-switch" onClick={() => { setAuthError(''); setModal(modal === 'login' ? 'signup' : 'login') }}>{modal === 'login' ? 'New to TaskFlow? Create an account' : 'Already have an account? Sign in'}</button></section></div>}
      {modal === 'project' && <div className="modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) setModal(null) }}><section className="modal-card" role="dialog" aria-modal="true" aria-labelledby="project-title"><div className="modal-heading"><div><span className="modal-kicker">WORKSPACE</span><h2 id="project-title">Create a project</h2></div><button className="icon-button" aria-label="Close" onClick={() => setModal(null)}><X size={18} /></button></div><form onSubmit={createProject}><label><span className="form-label">Project name</span><input className="form-control" value={newProjectName} onChange={(event) => setNewProjectName(event.target.value)} required maxLength={120} /></label><label><span className="form-label">Description</span><input className="form-control" value={newProjectDescription} onChange={(event) => setNewProjectDescription(event.target.value)} maxLength={800} /></label><div className="modal-actions"><button type="button" className="button-secondary" onClick={() => setModal(null)}>Cancel</button><button type="submit" className="button-primary">Create project</button></div></form></section></div>}
      {modal === 'invite' && <div className="modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) setModal(null) }}><section className="modal-card" role="dialog" aria-modal="true" aria-labelledby="invite-title"><div className="modal-heading"><div><span className="modal-kicker">{activeProject}</span><h2 id="invite-title">Add a teammate</h2></div><button className="icon-button" aria-label="Close" onClick={() => setModal(null)}><X size={18} /></button></div><form onSubmit={inviteMember}><label><span className="form-label">Account email</span><input className="form-control" type="email" value={inviteEmail} onChange={(event) => setInviteEmail(event.target.value)} required placeholder="teammate@company.com" /></label><p className="detail-description">The person must have a TaskFlow account. New accounts can sign up from the sign-in dialog.</p><div className="modal-actions"><button type="button" className="button-secondary" onClick={() => setModal(null)}>Cancel</button><button type="submit" className="button-primary">Add to project</button></div></form></section></div>}
      {modal === 'profile' && <div className="modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) setModal(null) }}><section className="modal-card" role="dialog" aria-modal="true" aria-labelledby="profile-title"><div className="modal-heading"><div><span className="modal-kicker">ACCOUNT</span><h2 id="profile-title">Edit profile</h2></div><button className="icon-button" aria-label="Close" onClick={() => setModal(null)}><X size={18} /></button></div><form onSubmit={saveProfile}><label><span className="form-label">Display name</span><input className="form-control" value={profileName} onChange={(event) => setProfileName(event.target.value)} required maxLength={90} /></label><div className="modal-actions"><button type="button" className="button-secondary" onClick={() => setModal(null)}>Cancel</button><button type="submit" className="button-primary">Save profile</button></div></form></section></div>}
      {toast && <div role="status" className="toast-message"><Check size={15} />{toast}</div>}
    </main>
  )
}
