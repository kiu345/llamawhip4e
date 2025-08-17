"use strict";

var editor;
var suggestions;

const words = [
	{ word: '/Discuss', icon: 'fas fa-apple-alt' },
	{ word: '/Document', icon: 'fas fa-banana' },
	{ word: '/Fix Errors', icon: 'fas fa-cherry' },
	{ word: '/Git Comment', icon: 'far fa-calendar-alt' },
	{ word: '/Refactor', icon: 'fas fa-seedling' },
	{ word: '/JUnit Test case', icon: 'fas fa-leaf' },
	{ word: '/Upgrade Source', icon: 'fas fa-wine-glass' }
];

let selectedIndex = -1;
let isAutoCompleting = false;

document.addEventListener('click', function(e) {
	if (e.target !== editor && !suggestions.contains(e.target)) {
		hideSuggestions();
	}
});

window.addEventListener('load', function() {
	addKeyCapture(editor);
});

function addKeyCapture() {
	editor = document.getElementById('inputarea');
	if (editor == null) {
		console.log('input element not found!');
		return;
	}

	suggestions = document.getElementById('suggestions');

	editor.focus();
	editor.addEventListener('input', handleInput);
	editor.addEventListener('keydown', handleKeyDown);
	console.log('input element bound');
}

function setPredefinedPrompt(command) {
	editor.innerHTML = command;
	editor.setAttribute('contenteditable', 'false');
	editor.removeEventListener('keydown');
	editor.classList.remove("current");
	editor.removeAttribute("autofocus");
}

function addAttachment(name) {
	const newItem = document.createElement('li');
	newItem.className = 'file-item';
	newItem.textContent  = name;
	console.log("adding attachment");
	document.getElementById('attachments').appendChild(newItem);
}

function toggelView(id) {
	const element = document.getElementById(id);
	if (element.style.display == 'none') {
		element.style.display = 'block';
	}
	else {
		element.style.display = 'none';
	}
}

function handleInput() {
	const selection = window.getSelection();
	const range = selection.getRangeAt(0);
	const node = range.startContainer;

	if (node.nodeType === Node.TEXT_NODE) {
		const text = node.textContent;
		const cursorPosition = range.startOffset;
		const lastWord = text.slice(0, cursorPosition).split(/\s+/).pop();

		if (lastWord.length > 0) {
			const matchedWords = words.filter(item => item.word.toUpperCase().startsWith(lastWord.toUpperCase()));
			showSuggestions(matchedWords, lastWord);
		} else {
			hideSuggestions();
		}
	}
}

function handleKeyDown(e) {
	//	if (suggestions.style.display === 'block') {
	if (isAutoCompleting) {
		switch (e.key) {
			case 'ArrowDown':
				e.preventDefault();
				selectNextSuggestion();
				break;
			case 'ArrowUp':
				e.preventDefault();
				selectPreviousSuggestion();
				break;
			case 'Enter':
				e.preventDefault();
				const selected = suggestions.querySelector('.selected');
				if (selected) {
					selected.click();
				}
				break;
		}
	}
	else {
		if (e.shiftKey) {
			
		}
		else if (e.altKey) {
			
		}
		else if (e.ctrlKey) {
			
		}
		else {
			switch (e.key) {
				case 'Enter':
					editor.removeEventListener('input', this);
					editor.removeEventListener('keydown', this);
					eclipseSendPrompt(editor.innerText, checkPredefinedPrompt(editor.innerText));
					document.getElementById('content').removeChild(document.getElementById('edit_area'))
/*
					editor.setAttribute('contenteditable', 'false');
					editor.parentElement.removeChild(document.getElementById('context'))
					editor.classList.remove("current");
					editor.removeAttribute("autofocus");
*/
					break;
			}
		}
	}
}

function checkPredefinedPrompt(command) {
	let found = false;
	words.forEach((word, index) => {
		if (word.word.indexOf(command) == 0) {
			console.log("input command = " + command);
			found = true;
		}
	});

	return found;
}

function showSuggestions(matchedWords, lastWord) {
	isAutoCompleting = true;
	suggestions.innerHTML = '';
	selectedIndex = -1;
	if (matchedWords.length > 0) {
		matchedWords.forEach((item, index) => {
			const div = document.createElement('div');
			div.className = 'suggestion';
			div.innerHTML = `<i class="${item.icon}"></i>${item.word}`;
			div.addEventListener('click', () => {
				replaceWord(lastWord, item.word);
				hideSuggestions();
			});
			suggestions.appendChild(div);
		});
		suggestions.style.display = 'block';
		positionSuggestions();
	} else {
		hideSuggestions();
	}
}

function hideSuggestions() {
	isAutoCompleting = false;
	suggestions.style.display = 'none';
	selectedIndex = -1;
}

function positionSuggestions() {
	const rect = editor.getBoundingClientRect();
	const sgtRect = suggestions.getBoundingClientRect();

	console.log(`rect = ${rect.top}, ${rect.bottom}`);
	console.log(`window.innerHeight = ${window.innerHeight}`);
	console.log(`suggestions.height = ${sgtRect.bottom} - ${sgtRect.top}`);

	if (rect.bottom + sgtRect.height > window.innerHeight)
		suggestions.style.top = `${rect.top - sgtRect.height + window.scrollY}px`;
	else
		suggestions.style.top = `${rect.bottom + window.scrollY}px`;

	suggestions.style.left = `${rect.left}px`;
}

function replaceWord(oldWord, newWord) {
	const selection = window.getSelection();
	const range = selection.getRangeAt(0);
	const node = range.startContainer;

	if (node.nodeType === Node.TEXT_NODE) {
		const text = node.textContent;
		const cursorPosition = range.startOffset;
		const start = text.slice(0, cursorPosition).lastIndexOf(oldWord);
		if (start !== -1) {
			const newText = text.slice(0, start) + newWord + text.slice(cursorPosition);
			node.textContent = newText;

			const newRange = document.createRange();
			newRange.setStart(node, start + newWord.length);
			newRange.setEnd(node, start + newWord.length);
			selection.removeAllRanges();
			selection.addRange(newRange);
		}
	}
}

function selectNextSuggestion() {
	const items = suggestions.querySelectorAll('.suggestion');
	if (selectedIndex < items.length - 1) {
		selectedIndex++;
		updateSelection();
	}
}

function selectPreviousSuggestion() {
	if (selectedIndex > 0) {
		selectedIndex--;
		updateSelection();
	}
}

function updateSelection() {
	const items = suggestions.querySelectorAll('.suggestion');
	items.forEach((item, index) => {
		if (index === selectedIndex) {
			item.classList.add('selected');
			item.scrollIntoView({ block: 'nearest' });
		} else {
			item.classList.remove('selected');
		}
	});
}


function enableDnD(id) {
	const dropZone = document.getElementById(id);

	dropZone.addEventListener('dragover', (e) => {
		e.preventDefault();
		dropZone.classList.add('hover');
	});

	dropZone.addEventListener('dragleave', () => {
		dropZone.classList.remove('hover');
	});

	dropZone.addEventListener('drop', (e) => {
		e.preventDefault();
		dropZone.classList.remove('hover');

		if (e.dataTransfer.files.length > 0) {
			for (const file of e.dataTransfer.files) {
				console.log('Datei:', file.name, file.size, 'bytes');
			}
			dropZone.textContent = e.dataTransfer.files.length + " Datei(en) gedropped";
		}
		else if (e.dataTransfer.types.includes('text/plain')) {
			e.dataTransfer.getData('text/plain'); 
			const text = e.dataTransfer.getData('text/plain');
			console.log('Text:', text);
			dropZone.textContent = "Text gedropped: " + text;
		}
	});
}